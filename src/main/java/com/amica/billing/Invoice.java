package com.amica.billing;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Simple JavaBean representing an invoice.
 *
 * @author Will Provost
 */
@Data
@EqualsAndHashCode(of="number")
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
	private int number;
    private Customer customer;
    private double amount;
    private LocalDate theDate;
    private LocalDate paidDate;

    @Override
    public String toString() {
    	return "Invoice " + number;
    }

	/**
	 * Helper to determine whether this invoice is overdue: based on the
	 * payment terms of the associated customer, was this invoice paid on time?
	 * Invoices that have not yet been paid are evaluated based on the given
	 * "as of" date -- as in, are they overdue as of now? 
	 */
	public boolean isOverdue(LocalDate asOf) {
		LocalDate endDate = paidDate != null ? paidDate : asOf;
		int daysAllowed = customer.getTerms().getDays();
		return endDate.isAfter(theDate.plusDays(daysAllowed));		
	}
	
}
