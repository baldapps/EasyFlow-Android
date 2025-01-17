EasyFlow-Android
========
This work is just conversion of EasyFlow forked from [EasyFlow](https://github.com/carrot-garden/state_EasyFlow)

EasyFlow-Android has the following:
* Implement standard Logging from Android **android.util.Log** instead of **org.slf4j.Logger**
* Add check whether Event has State to map to.
* Life-cycle aware


For more info on EasyFlow refer to original repository : [EasyFlow](https://github.com/Beh01der/EasyFlow)


README from source
------------

EasyFlow is a simple and lightweight Finite State Machine for Java

With **EasyFlow** you can:
* implement complex logic but keep your code simple and clean
* handle asynchronous calls with ease and elegance
* avoid concurrency by using event-driven programming approach
* avoid *StackOverflow* error by avoiding recursion
* simplify design, programming and testing of complex java applications

All this in less then 30kB and no run-time overhead!

Here is a simple example illustrating how a state machine can be definded and implemented with **EasyFlow**

This is a State diargam fragment describing a simple ATM workflow

![State diagram fragment](http://datasymphony.com.au/wp-content/uploads/2013/04/atm_example.png)

With **EasyFlow** we can define the above state machine like this

```java
EasyFlow flow = FlowBuilder
    .from(SHOWING_WELCOME, "myflow").transit(
        onCardPresent.to(WAITING_FOR_PIN).transit(
            onPinProvided.to(...).transit(
                ...
            ),
            onCancel.to(RETURNING_CARD).transit(
                onCardExtracted.to(SHOWING_WELCOME)
            )
        )
    )
```
then all what's left to do is to implement our state handlers like so
```java
SHOWING_WELCOME.whenEnter((state, context) -> {
        ...
        btnOption1.setOnClickListener(v -> {
            onCardPresent.trigger(context);
        });
        ...
    }
});
...
```
and start the flow
```java
flow.start(new FlowContext());
```
See the [complete example](https://github.com/baldapps/EasyFlow-Android/blob/master/easyflowdemo/src/main/java/com/apipas/android/easyflowdemo/MainActivity.java)
