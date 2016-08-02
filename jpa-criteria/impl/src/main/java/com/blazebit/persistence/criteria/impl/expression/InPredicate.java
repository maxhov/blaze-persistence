package com.blazebit.persistence.criteria.impl.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Subquery;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.TypeConverter;
import com.blazebit.persistence.criteria.impl.TypeUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InPredicate<T> extends AbstractSimplePredicate implements BlazeCriteriaBuilderImpl.In<T> {

    private static final long serialVersionUID = 1L;

    private final Expression<? extends T> expression;
    private final List<Expression<? extends T>> values;

    public InPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends T> expression) {
        this(criteriaBuilder, expression, new ArrayList<Expression<? extends T>>());
    }

    public InPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends T> expression, Expression<? extends T>... values) {
        this(criteriaBuilder, expression, Arrays.asList(values));
    }

    public InPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends T> expression, T... values) {
        this(criteriaBuilder, expression, new ArrayList<T>(Arrays.asList(values)));
    }

    public InPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends T> expression, Collection<T> values) {
        super(criteriaBuilder, false);
        this.expression = expression;
        this.values = new ArrayList<Expression<? extends T>>(values.size());
        final Class<? extends T> javaType = expression.getJavaType();

        if (javaType == null || !TypeUtils.isNumeric(javaType)) {
            for (T value : values) {
                this.values.add(new LiteralExpression<T>(criteriaBuilder, value));
            }
        } else {
            TypeConverter<? extends T> converter = TypeUtils.getConverter((Class<? extends T>) javaType);
            for (T value : values) {
                this.values.add(new LiteralExpression<T>(criteriaBuilder, converter.convert(value)));
            }
        }
    }

    private InPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends T> expression, List<Expression<? extends T>> values) {
        super(criteriaBuilder, false);

        if (expression instanceof ParameterExpressionImpl<?>) {
            throw new IllegalArgumentException("A parameter can't be the left hand expression of an in predicate!");
        }

        this.expression = expression;
        this.values = values;

        for (int i = 0; i < values.size(); i++) {
            Expression<? extends T> expr = values.get(i);
            if (expr == null) {
                throw new IllegalArgumentException("Null expression at index: " + (i + 1));
            }
        }
    }

    // Copy-constructor
    private InPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<? extends T> expression, List<Expression<? extends T>> values) {
        super(criteriaBuilder, negated);
        this.expression = expression;
        this.values = values;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new InPredicate<T>(criteriaBuilder, !isNegated(), expression, values);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Expression<T> getExpression() {
        return (Expression<T>) expression;
    }

    @Override
    public InPredicate<T> value(T value) {
        return value(new LiteralExpression<T>(criteriaBuilder, value));
    }

    @Override
    public InPredicate<T> value(Expression<? extends T> value) {
        values.add(value);
        return this;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(expression);

        for (Expression<?> expr : values) {
            visitor.visit(expr);
        }
    }

    @Override
    public void render(RenderContext context) {
        List<Expression<? extends T>> values = this.values;
        StringBuilder buffer = context.getBuffer();

        switch (values.size()) {
            case 0:
                if (isNegated()) {
                    buffer.append("1=1");
                } else {
                    buffer.append("1=0");
                }
                return;
            case 1:
                Expression<? extends T> first = values.get(0);
                if (first instanceof Subquery<?> || (first instanceof ParameterExpressionImpl<?> && Collection.class.isAssignableFrom(((ParameterExpressionImpl<?>) first).getParameterType()))) {
                    context.apply(expression);

                    if (isNegated()) {
                        buffer.append(" NOT");
                    }

                    buffer.append(" IN ");
                    context.apply(first);
                    return;
                }
            default:
                context.apply(expression);

                if (isNegated()) {
                    buffer.append(" NOT");
                }

                buffer.append(" IN ");
                buffer.append('(');
                for (int i = 0; i < values.size(); i++) {
                    if (i != 0) {
                        buffer.append(',');
                    }
                    context.apply(values.get(i));
                }
                buffer.append(')');
        }
    }
    
}
