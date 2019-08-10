package com.apipas.easyflow.android;



public class TransitionBuilder {

	private Event event;
	private State stateTo;
	
	protected TransitionBuilder(Event event, State stateTo) {
		this.event = event;
		this.stateTo = stateTo;
	}
	
	protected Event getEvent() {
		return event;
	}

	protected State getStateTo() {
		return stateTo;
	}

	public TransitionBuilder transit(TransitionBuilder... transitions) {
		for (TransitionBuilder transition : transitions) {
            transition.getEvent().addTransition(stateTo, transition.getStateTo());
			stateTo.addEvent(transition.getEvent(), transition.getStateTo());
		}
		return this;
	}
}
