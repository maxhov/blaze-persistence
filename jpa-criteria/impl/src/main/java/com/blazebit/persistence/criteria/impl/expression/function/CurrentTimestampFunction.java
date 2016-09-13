package com.blazebit.persistence.criteria.impl.expression.function;

import java.sql.Timestamp;
import java.util.Calendar;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

import javax.persistence.criteria.Expression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CurrentTimestampFunction extends AbstractFunctionExpression<Timestamp> {

    private static final long serialVersionUID = 1L;
    
    public static final String NAME = "CURRENT_TIMESTAMP";

    public CurrentTimestampFunction(BlazeCriteriaBuilderImpl criteriaBuilder) {
        super(criteriaBuilder, Timestamp.class, NAME);
    }

    @Override
    public void render(RenderContext context) {
        context.getBuffer().append(getFunctionName());
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public <X> Expression<X> as(Class<X> type) {
        if (java.util.Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type)) {
            return (Expression<X>) this;
        }
        return super.as(type);
    }
}