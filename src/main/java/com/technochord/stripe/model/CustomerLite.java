package com.technochord.stripe.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter

/**
 * Represents a 'lite' version of the {@Link Customer} object.
 * This object contains the customerId and this object can be used to retrieve the
 * full {@link Customer}.
 *
 * Besides the customerId, this class contains the email, description and delinquent(boolean)
 */
public class CustomerLite {
    private String id;
    private String email;
    private String description;
    private boolean delinquent;
}
