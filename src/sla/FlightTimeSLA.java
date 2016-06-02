package sla;

public class FlightTimeSLA {
	
	private int inspectionDaysPerMonth, inspectionMinutesPerDay;
	
	public FlightTimeSLA() {
		this(30, 1440);
	}

	public FlightTimeSLA(int inspectionDaysPerMonth, int inspectionMinutesPerDay) {
		setInspectionDaysPerMonth(inspectionDaysPerMonth);
		setInspectionMinutesPerDay(inspectionMinutesPerDay);
	}

	public void setInspectionDaysPerMonth(int inspectionDaysPerMonth) {
		if(inspectionDaysPerMonth <= 0)
			throw new IllegalArgumentException("The inspection days per month must be strictly positive");
		this.inspectionDaysPerMonth = inspectionDaysPerMonth;
	}

	public void setInspectionMinutesPerDay(int inspectionMinutesPerDay) {
		if(inspectionMinutesPerDay <= 0)
			throw new IllegalArgumentException("The inspection minutes per day must be strictly positive");
		this.inspectionMinutesPerDay = inspectionMinutesPerDay;
	}
	
	public boolean isUAVFlyTime(long time) {
		return time % (1440*30) < (inspectionDaysPerMonth*1440) && time % 1440 < inspectionMinutesPerDay;
	}


}
