package com.apipas.android.easyflowdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.apipas.easyflow.android.EasyFlow;
import com.apipas.easyflow.android.Event;
import com.apipas.easyflow.android.FlowBuilder;
import com.apipas.easyflow.android.FlowContext;
import com.apipas.easyflow.android.State;

import androidx.annotation.NonNull;


public class MainActivity extends Activity {
    private TextView txtCaption;
    private TextView txtMessage;
    private EditText txtInput;
    private Button btnOption1;
    private Button btnOption2;
    private Button btnOption3;
    private Button btnOption4;

    /*
     * We can extend flow context to add information to the basic
     * context or we can just use fields as activity attributes.
     * If we decide to extend FlowContext the attributes can
     * be reconfigured since FlowContext is Parcelable.
     */
    public static class MyContext extends FlowContext {
        private TextWatcher textWatcher;
        private String pin;
        private int invalidPinCounter;
        private int balance = 1000;
        private int withdrawAmt;

        public MyContext() {
        }

        protected MyContext(Parcel in) {
            super(in);
            pin = in.readString();
            invalidPinCounter = in.readInt();
            balance = in.readInt();
            withdrawAmt = in.readInt();
        }

        public static final Creator<MyContext> CREATOR = new Creator<MyContext>() {
            @Override
            public MyContext createFromParcel(Parcel in) {
                return new MyContext(in);
            }

            @Override
            public MyContext[] newArray(int size) {
                return new MyContext[size];
            }
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(pin);
            dest.writeInt(invalidPinCounter);
            dest.writeInt(balance);
            dest.writeInt(withdrawAmt);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static MyContext to(FlowContext f) {
            return cast(f, MyContext.class);
        }
    }

    // defining states
    private final State SHOWING_WELCOME = FlowBuilder.state("SHOWING_WELCOME");
    private final State WAITING_FOR_PIN = FlowBuilder.state("WAITING_FOR_PIN");
    private final State CHECKING_PIN = FlowBuilder.state("CHECKING_PIN");
    private final State RETURNING_CARD = FlowBuilder.state("RETURNING_CARD");
    private final State SHOWING_MAIN_MENU = FlowBuilder.state("SHOWING_MAIN_MENU");
    private final State SHOWING_PIN_INVALID = FlowBuilder.state("SHOWING_PIN_INVALID");
    private final State SHOWING_CARD_LOCKED = FlowBuilder.state("SHOWING_CARD_LOCKED");
    private final State SHOWING_BALANCE = FlowBuilder.state("SHOWING_BALANCE");
    private final State SHOWING_WITHDRAW_MENU = FlowBuilder.state("SHOWING_WITHDRAW_MENU");
    private final State SHOWING_TAKE_CASH = FlowBuilder.state("SHOWING_TAKE_CASH");

    // defining events
    private final Event onCardPresent = FlowBuilder.event();
    private final Event onCardExtracted = FlowBuilder.event();
    private final Event onPinProvided = FlowBuilder.event();
    private final Event onPinValid = FlowBuilder.event();
    private final Event onPinInvalid = FlowBuilder.event();
    private final Event onTryAgain = FlowBuilder.event();
    private final Event onNoMoreTries = FlowBuilder.event();
    private final Event onCancel = FlowBuilder.event();
    private final Event onConfirm = FlowBuilder.event();
    private final Event onMenuShowBalance = FlowBuilder.event();
    private final Event onMenuWithdrawCash = FlowBuilder.event();
    private final Event onMenuExit = FlowBuilder.event();
    private final Event onCashExtracted = FlowBuilder.event();

    private EasyFlow flow;

    private void initFlow() {
        if (flow != null) {
            return;
        }

        // building our FSM
        flow = FlowBuilder
                .from(SHOWING_WELCOME, "myflow").transit(
                        onCardPresent.to(WAITING_FOR_PIN).transit(
                                onPinProvided.to(CHECKING_PIN).transit(
                                        onPinValid.to(SHOWING_MAIN_MENU).transit(
                                                onMenuShowBalance.to(SHOWING_BALANCE).transit(
                                                        onCancel.to(SHOWING_MAIN_MENU)
                                                ),
                                                onMenuWithdrawCash.to(SHOWING_WITHDRAW_MENU).transit(
                                                        onCancel.to(SHOWING_MAIN_MENU),
                                                        onConfirm.to(SHOWING_TAKE_CASH).transit(
                                                                onCashExtracted.to(SHOWING_MAIN_MENU)
                                                        )
                                                ),
                                                onMenuExit.to(RETURNING_CARD)
                                        ),
                                        onPinInvalid.to(SHOWING_PIN_INVALID).transit(
                                                onTryAgain.to(WAITING_FOR_PIN),
                                                onNoMoreTries.to(SHOWING_CARD_LOCKED).transit(
                                                        onConfirm.to(SHOWING_WELCOME)
                                                ),
                                                onCancel.to(RETURNING_CARD)
                                        )
                                ),
                                onCancel.to(RETURNING_CARD).transit(
                                        onCardExtracted.to(SHOWING_WELCOME)
                                )
                        )
                );
    }

    @SuppressLint("SetTextI18n")
    private void bindFlow() {
        SHOWING_WELCOME.whenEnter((state, context) -> {
            txtCaption.setText("Welcome");
            txtMessage.setText("Welcome to our ATM\nPlease insert your card");
            txtInput.setVisibility(View.GONE);
            btnOption1.setText("Insert a Card");
            btnOption1.setVisibility(View.VISIBLE);
            btnOption1.setOnClickListener(v -> onCardPresent.trigger(context));
            btnOption2.setVisibility(View.GONE);
            btnOption3.setVisibility(View.GONE);
            btnOption4.setVisibility(View.GONE);
            MyContext.to(context).invalidPinCounter = 0;
        });

        WAITING_FOR_PIN.whenEnter((state, context) -> {
            MyContext.to(context).textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    btnOption1.setEnabled(s.length() == 4);
                }
            };

            txtCaption.setText("Waiting for PIN");
            txtMessage.setText("Please enter your PIN and press 'Continue'\n(BTW current PIN is 1234)");
            txtInput.setText("");
            txtInput.setVisibility(View.VISIBLE);
            txtInput.addTextChangedListener(MyContext.to(context).textWatcher);
            btnOption1.setText("Continue");
            btnOption1.setVisibility(View.VISIBLE);
            btnOption1.setEnabled(false);
            btnOption1.setOnClickListener(v -> {
                MyContext.to(context).pin = txtInput.getText().toString();
                onPinProvided.trigger(context);
            });
            btnOption2.setText("Cancel");
            btnOption2.setVisibility(View.VISIBLE);
            btnOption2.setOnClickListener(v -> onCancel.trigger(context));
            btnOption3.setVisibility(View.GONE);
            btnOption4.setVisibility(View.GONE);
        }).whenLeave((state, context) -> {
            txtInput.removeTextChangedListener(MyContext.to(context).textWatcher);
            btnOption1.setEnabled(true);
        });

        CHECKING_PIN.whenEnter((state, context) -> {
            if (MyContext.to(context).pin.equals("1234")) {
                onPinValid.trigger(context);
            } else {
                MyContext.to(context).invalidPinCounter++;
                onPinInvalid.trigger(context);
            }
        });

        SHOWING_MAIN_MENU.whenEnter((state, context) -> {
            txtCaption.setText("Main Menu");
            txtMessage.setText("I want to:");
            txtInput.setVisibility(View.GONE);
            btnOption1.setText("See balance");
            btnOption1.setVisibility(View.VISIBLE);
            btnOption1.setOnClickListener(v -> onMenuShowBalance.trigger(context));
            btnOption2.setText("Get cash");
            btnOption2.setVisibility(View.VISIBLE);
            btnOption2.setOnClickListener(v -> onMenuWithdrawCash.trigger(context));
            btnOption3.setText("Exit");
            btnOption3.setOnClickListener(v -> onMenuExit.trigger(context));
            btnOption3.setVisibility(View.VISIBLE);
            btnOption4.setVisibility(View.GONE);
        });

        SHOWING_BALANCE.whenEnter((state, context) -> {
            txtCaption.setText("Showing Balance");
            txtMessage.setText("You currently have $" + MyContext.to(context).balance + " on your account");
            txtInput.setVisibility(View.GONE);
            btnOption1.setText("Ok");
            btnOption1.setVisibility(View.VISIBLE);
            btnOption1.setOnClickListener(v -> onCancel.trigger(context));
            btnOption2.setVisibility(View.GONE);
            btnOption3.setVisibility(View.GONE);
            btnOption4.setVisibility(View.GONE);
        });

        SHOWING_WITHDRAW_MENU.whenEnter((state, context) -> {
            txtCaption.setText("Withdraw Cash");
            txtMessage.setText("How much cash do you want to withdraw?");
            txtInput.setVisibility(View.GONE);
            btnOption1.setText("$50");
            btnOption1.setVisibility(View.VISIBLE);
            btnOption1.setEnabled(MyContext.to(context).balance > 50);
            btnOption1.setOnClickListener(v -> {
                MyContext.to(context).withdrawAmt = 50;
                onConfirm.trigger(context);
            });
            btnOption2.setText("$100");
            btnOption2.setVisibility(View.VISIBLE);
            btnOption2.setEnabled(MyContext.to(context).balance > 100);
            btnOption2.setOnClickListener(v -> {
                MyContext.to(context).withdrawAmt = 100;
                onConfirm.trigger(context);
            });
            btnOption3.setText("$200");
            btnOption3.setVisibility(View.VISIBLE);
            btnOption3.setEnabled(MyContext.to(context).balance > 200);
            btnOption3.setOnClickListener(v -> {
                MyContext.to(context).withdrawAmt = 200;
                onConfirm.trigger(context);
            });
            btnOption4.setText("Cancel");
            btnOption4.setVisibility(View.VISIBLE);
            btnOption4.setOnClickListener(v -> onCancel.trigger(context));
        }).whenLeave((state, context) -> {
            btnOption1.setEnabled(true);
            btnOption2.setEnabled(true);
            btnOption3.setEnabled(true);
        });

        SHOWING_TAKE_CASH.whenEnter((state, context) -> {
            txtCaption.setText("Take your cash");
            txtMessage.setText("Please, take your cash");
            txtInput.setVisibility(View.GONE);
            btnOption1.setText("Take my $" + MyContext.to(context).withdrawAmt);
            btnOption1.setVisibility(View.VISIBLE);
            btnOption1.setOnClickListener(v -> {
                MyContext.to(context).balance -= MyContext.to(context).withdrawAmt;
                onCashExtracted.trigger(context);
            });
            btnOption2.setVisibility(View.GONE);
            btnOption3.setVisibility(View.GONE);
            btnOption4.setVisibility(View.GONE);
        });

        SHOWING_PIN_INVALID.whenEnter((state, context) -> {
            boolean canTryAgain = MyContext.to(context).invalidPinCounter < 3;

            txtCaption.setText("Invalid PIN");
            txtMessage.setText("You entered invalid PIN.\n(" + (3 - MyContext.to(context).invalidPinCounter) + " " +
                    "attempts left)");
            if (canTryAgain) {
                btnOption1.setText("Try Again");
                btnOption1.setOnClickListener(v -> onTryAgain.trigger(context));
                btnOption2.setText("Cancel");
                btnOption2.setOnClickListener(v -> onCancel.trigger(context));
                btnOption2.setVisibility(View.VISIBLE);
            } else {
                btnOption1.setText("Ok");
                btnOption1.setOnClickListener(v -> onNoMoreTries.trigger(context));
                btnOption2.setVisibility(View.GONE);
            }

            btnOption1.setVisibility(View.VISIBLE);
            txtInput.setVisibility(View.GONE);
            btnOption3.setVisibility(View.GONE);
            btnOption4.setVisibility(View.GONE);
        });

        SHOWING_CARD_LOCKED.whenEnter((state, context) -> {
            txtCaption.setText("Your Card has been locked");
            txtMessage.setText("You have entered invalid PIN 3 times so I swallowed your card.\n" +
                    "Mmm... Yummy ;)");
            txtInput.setVisibility(View.GONE);
            btnOption1.setText("Ok");
            btnOption1.setVisibility(View.VISIBLE);
            btnOption1.setOnClickListener(v -> onConfirm.trigger(context));
            btnOption2.setVisibility(View.GONE);
            btnOption3.setVisibility(View.GONE);
            btnOption4.setVisibility(View.GONE);
        });

        RETURNING_CARD.whenEnter((state, context) -> {
            txtCaption.setText("Returning Card");
            txtMessage.setText("Thanks for using our ATM\nPlease take your card");
            txtInput.setVisibility(View.GONE);
            btnOption1.setText("Take the Card");
            btnOption1.setVisibility(View.VISIBLE);
            btnOption1.setOnClickListener(v -> onCardExtracted.trigger(context));
            btnOption2.setVisibility(View.GONE);
            btnOption3.setVisibility(View.GONE);
            btnOption4.setVisibility(View.GONE);
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        txtCaption = findViewById(R.id.txtCaption);
        txtMessage = findViewById(R.id.txtMessage);
        txtInput = findViewById(R.id.txtInput);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);

        initFlow();
        bindFlow();

        if (savedInstanceState != null) {
            flow.onRestoreInstanceState(savedInstanceState);
            flow.start();
        } else {
            flow.start(new MyContext());
        }
    }
    
    //NOTE: CALL THIS METHOD TO RECONFIGURE!!
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        flow.onSaveInstanceState(outState);
    }
}
