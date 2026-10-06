package com.example.restlab.model.dto.post;

import java.io.Serializable;

/**
 * Projection Interface for Native SQL Aggregation Queries (Experiment 2.2.2)
 */
public interface PostSummaryDto extends Serializable {

    String getStatus();

    Long getTotalCount();
}
