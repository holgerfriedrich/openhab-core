/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.automation.internal.module.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.handler.BaseConditionModuleHandler;
import org.openhab.core.automation.internal.module.exception.UncomparableException;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Comparation Condition
 *
 * @author Benedikt Niehues - Initial contribution
 */
@NonNullByDefault
public class CompareConditionHandler extends BaseConditionModuleHandler {

    public static final String MODULE_TYPE = "core.GenericCompareCondition";

    public static final String INPUT_LEFT_OBJECT = "input";
    public static final String INPUT_LEFT_FIELD = "inputproperty";
    public static final String RIGHT_OP = "right";
    public static final String OPERATOR = "operator";

    public final Logger logger = LoggerFactory.getLogger(CompareConditionHandler.class);

    public CompareConditionHandler(Condition module) {
        super(module);
    }

    @Override
    public boolean isSatisfied(Map<String, @Nullable Object> context) {
        Object operatorObj = this.module.getConfiguration().get(OPERATOR);
        String operator = operatorObj instanceof String s ? s : null;
        Object rightObj = this.module.getConfiguration().get(RIGHT_OP);
        String rightOperandString = rightObj instanceof String s ? s : null;
        Object leftObjFieldNameObj = this.module.getConfiguration().get(INPUT_LEFT_FIELD);
        String leftObjectFieldName = leftObjFieldNameObj instanceof String s ? s : null;
        if (rightOperandString == null || operator == null) {
            return false;
        } else {
            Object leftObj = context.get(INPUT_LEFT_OBJECT);
            Object toCompare = getCompareValue(leftObj, leftObjectFieldName);
            Object rightValue = getRightOperandValue(rightOperandString, toCompare);
            if (rightValue == null) {
                if (leftObj != null) {
                    logger.info("unsupported type for compare condition: {}", leftObj.getClass());
                } else {
                    logger.info("unsupported type for compare condition: null ({})",
                            module.getInputs().get(INPUT_LEFT_FIELD));
                }
                return false;
            }
            try {
                switch (operator) {
                    case "eq":
                    case "EQ":
                    case "=":
                    case "==":
                    case "equals":
                    case "EQUALS":
                        // EQUALS
                        if (toCompare == null) {
                            return "null".equals(rightOperandString) || "".equals(rightOperandString);
                        } else {
                            return toCompare.equals(rightValue);
                        }
                    case "gt":
                    case "GT":
                    case ">":
                        // Greater
                        return (toCompare != null) && (compare(toCompare, rightValue) > 0);
                    case "gte":
                    case "GTE":
                    case ">=":
                    case "=>":
                        // Greater or equal
                        return (toCompare != null) && (compare(toCompare, rightValue) >= 0);
                    case "lt":
                    case "LT":
                    case "<":
                        return (toCompare != null) && (compare(toCompare, rightValue) < 0);
                    case "lte":
                    case "LTE":
                    case "<=":
                    case "=<":
                        return (toCompare != null) && (compare(toCompare, rightValue) <= 0);
                    case "matches":
                        // Matcher...
                        if (toCompare instanceof String string1 && rightValue instanceof String string2) {
                            return string1.matches(string2);
                        }
                    default:
                        break;
                }
            } catch (UncomparableException e) {
                // values can not be compared, so assume that the condition is not satisfied
                return false;
            }

            return false;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private int compare(Object a, Object b) throws UncomparableException {
        if (Comparable.class.isAssignableFrom(a.getClass()) && a.getClass().equals(b.getClass())) {
            try {
                return ((Comparable) a).compareTo(b);
            } catch (ClassCastException e) {
                // should never happen but to be save here!
                throw new UncomparableException();
            }
        }
        throw new UncomparableException();
    }

    private @Nullable Object getRightOperandValue(String rightOperandString2, @Nullable Object toCompare) {
        if ("null".equals(rightOperandString2)) {
            return rightOperandString2;
        }
        if (toCompare instanceof State state) {
            return TypeParser.parseState(List.of(state.getClass()), rightOperandString2);
        } else if (toCompare instanceof Integer) {
            return Integer.parseInt(rightOperandString2);
        } else if (toCompare instanceof String) {
            return rightOperandString2;
        } else if (toCompare instanceof Long) {
            return Long.parseLong(rightOperandString2);
        } else if (toCompare instanceof Double) {
            return Double.parseDouble(rightOperandString2);
        }
        return null;
    }

    private @Nullable Object getCompareValue(@Nullable Object leftObj, @Nullable String leftObjFieldName) {
        if (leftObj == null || leftObjFieldName == null || leftObjFieldName.isEmpty() || leftObj instanceof String
                || leftObj instanceof Integer || leftObj instanceof Long || leftObj instanceof Double) {
            return leftObj;
        } else {
            try {
                Method m = leftObj.getClass().getMethod(
                        "get" + leftObjFieldName.substring(0, 1).toUpperCase() + leftObjFieldName.substring(1));
                return m.invoke(leftObj);
            } catch (NoSuchMethodException | SecurityException | StringIndexOutOfBoundsException
                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                return null;
            }
        }
    }
}
