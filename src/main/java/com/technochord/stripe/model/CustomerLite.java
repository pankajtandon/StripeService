package com.technochord.stripe.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CustomerLite {
    private String id;
    private String email;
    private String description;
    private boolean delinquent;
}
