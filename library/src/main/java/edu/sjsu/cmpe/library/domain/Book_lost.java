package edu.sjsu.cmpe.library.domain;

public class Book_lost extends Book {
	private boolean isLost = false;
	public Book_lost() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the isLost
	 */
	public boolean isLost() {
		return isLost;
	}
	/**
	 * @param status the isLost to set
	 */
	public void setLost(Status status) {
		if (status.equals(Status.lost))
			this.isLost = true;
		else
			this.isLost = false;
	}

}
