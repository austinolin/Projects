package edu.neu.ccs.cs8674.sp15.seattle.assignment4.problem2;
/**
 * This is a class called Date representing a calendar date with
 * time, month, day, and year
 * @author Austin
 *
 */
public class Date {

	private Integer year; 
	private Integer month;
	private Integer day;
	private Integer hours;
	private Integer minutes;
	/**
	 * Creates a new Date
	 * @param year, 4 digit positive/non-Null Integer representing the year
	 * @param month, 2 digit non-null Integer between 1-12 representing the month
	 * @param day, 2 digit non-null Integer between 1-31 representing the day
	 * @param hours, 2 digit non-null Integer within the range 1-24 representing the hour
	 * @param minutes, 2 digit non-null Integer between 1-59 representing the minute of the hour 
	 */
	public Date(Integer year, Integer month, Integer day, Integer hours, Integer minutes) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hours = hours;
		this.minutes = minutes;
	}
	/**
	 * Asks for the year of the Date
	 * @return the Integer value of the year
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * Asks for the month of the Date
	 * @return the Integer value of the month
	 */
	public Integer getMonth() {
		return month;
	}

	/**
	 * Asks for the day of the Date
	 * @return the Integer value of the day
	 */
	public Integer getDay() {
		return day;
	}

	/**
	 * Asks for the hour of the Date
	 * @return the Integer value of the hour
	 */
	public Integer getHours() {
		return hours;
	}

	/**
	 * Asks for the minute of the Date
	 * @return the Integer value of the minute
	 */
	public Integer getMinutes() {
		return minutes;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((hours == null) ? 0 : hours.hashCode());
		result = prime * result + ((minutes == null) ? 0 : minutes.hashCode());
		result = prime * result + ((month == null) ? 0 : month.hashCode());
		result = prime * result + ((year == null) ? 0 : year.hashCode());
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Date other = (Date) obj;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equals(other.day))
			return false;
		if (hours == null) {
			if (other.hours != null)
				return false;
		} else if (!hours.equals(other.hours))
			return false;
		if (minutes == null) {
			if (other.minutes != null)
				return false;
		} else if (!minutes.equals(other.minutes))
			return false;
		if (month == null) {
			if (other.month != null)
				return false;
		} else if (!month.equals(other.month))
			return false;
		if (year == null) {
			if (other.year != null)
				return false;
		} else if (!year.equals(other.year))
			return false;
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String sday = (day < 10 ? "0" : "") + day;
		String smonth = (month < 10 ? "0" : "") + month;
		String sminutes = (minutes < 10 ? "0" : "") + minutes;
		String shours = (hours < 10 ? "0" : "") + hours;
		return year + "." + smonth + "." + sday + " (" + shours + ":" + sminutes + ")";
	}
	
	
}
