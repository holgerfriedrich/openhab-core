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
package org.openhab.core.library.types;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupFunction;
import org.openhab.core.items.Item;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.Statistics;

/**
 * This interface is only a container for functions that require the core type library
 * for its calculations.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Thomas Eichstädt-Engelen - Added "N" functions
 * @author Gaël L'hopital - Added count function
 * @author Fabian Vollmann - Added XOR function
 */
@NonNullByDefault
public interface ArithmeticGroupFunction extends GroupFunction {

    /**
     * This does a logical 'and' operation. Only if all items are of 'activeState' this
     * is returned, otherwise the 'passiveState' is returned.
     *
     * Through the getStateAs() method, it can be determined, how many
     * items actually are not in the 'activeState'.
     */
    class And implements GroupFunction {

        protected final State activeState;
        protected final State passiveState;

        public And(@Nullable State activeValue, @Nullable State passiveValue) {
            if (activeValue == null || passiveValue == null) {
                throw new IllegalArgumentException("Parameters must not be null!");
            }
            this.activeState = activeValue;
            this.passiveState = passiveValue;
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            if (items == null || items.isEmpty()) {
                // if we do not have any items, we return the passive state
                return passiveState;
            } else {
                for (Item item : items) {
                    if (!activeState.equals(item.getStateAs(activeState.getClass()))) {
                        return passiveState;
                    }
                }
                return activeState;
            }
        }

        @Override
        public @Nullable <T extends State> T getStateAs(@Nullable Set<Item> items, Class<T> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return stateClass.cast(state);
            } else {
                if (stateClass == DecimalType.class) {
                    if (items != null) {
                        return stateClass.cast(new DecimalType(items.size() - count(items, activeState)));
                    } else {
                        return stateClass.cast(DecimalType.ZERO);
                    }
                } else {
                    return null;
                }
            }
        }

        private int count(Set<Item> items, State state) {
            int count = 0;
            for (Item item : items) {
                if (state.equals(item.getStateAs(state.getClass()))) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public State[] getParameters() {
            return new State[] { activeState, passiveState };
        }
    }

    /**
     * This does a logical 'or' operation. If at least one item is of 'activeState' this
     * is returned, otherwise the 'passiveState' is returned.
     *
     * Through the getStateAs() method, it can be determined, how many
     * items actually are in the 'activeState'.
     */
    class Or implements GroupFunction {

        protected final State activeState;
        protected final State passiveState;

        public Or(@Nullable State activeValue, @Nullable State passiveValue) {
            if (activeValue == null || passiveValue == null) {
                throw new IllegalArgumentException("Parameters must not be null!");
            }
            this.activeState = activeValue;
            this.passiveState = passiveValue;
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            if (items != null) {
                for (Item item : items) {
                    if (activeState.equals(item.getStateAs(activeState.getClass()))) {
                        return activeState;
                    }
                }
            }
            return passiveState;
        }

        @Override
        public @Nullable <T extends State> T getStateAs(@Nullable Set<Item> items, Class<T> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return stateClass.cast(state);
            } else {
                if (stateClass == DecimalType.class) {
                    if (items != null) {
                        return stateClass.cast(new DecimalType(count(items, activeState)));
                    } else {
                        return stateClass.cast(DecimalType.ZERO);
                    }
                } else {
                    return null;
                }
            }
        }

        private int count(Set<Item> items, State state) {
            int count = 0;
            for (Item item : items) {
                if (state.equals(item.getStateAs(state.getClass()))) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public State[] getParameters() {
            return new State[] { activeState, passiveState };
        }
    }

    /**
     * This does a logical 'nand' operation. The state is 'calculated' by
     * the normal 'and' operation and than negated by returning the opposite
     * value. E.g. when the 'and' operation calculates the activeValue the
     * passiveValue will be returned and vice versa.
     */
    class NAnd extends And {

        public NAnd(@Nullable State activeValue, @Nullable State passiveValue) {
            super(activeValue, passiveValue);
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            State result = super.calculate(items);
            return activeState.equals(result) ? passiveState : activeState;
        }
    }

    /**
     * This does a logical 'nor' operation. The state is 'calculated' by
     * the normal 'or' operation and than negated by returning the opposite
     * value. E.g. when the 'or' operation calculates the activeValue the
     * passiveValue will be returned and vice versa.
     */
    class NOr extends Or {

        public NOr(@Nullable State activeValue, @Nullable State passiveValue) {
            super(activeValue, passiveValue);
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            State result = super.calculate(items);
            return activeState.equals(result) ? passiveState : activeState;
        }
    }

    /**
     * This does a logical 'xor' operation. If exactly one item is of 'activeState' this
     * is returned, otherwise the 'passiveState' is returned.
     *
     * Through the getStateAs() method, it can be determined, how many
     * items actually are in the 'activeState'.
     */
    class Xor implements GroupFunction {

        protected final State activeState;
        protected final State passiveState;

        public Xor(@Nullable State activeValue, @Nullable State passiveValue) {
            if (activeValue == null || passiveValue == null) {
                throw new IllegalArgumentException("Parameters must not be null!");
            }
            this.activeState = activeValue;
            this.passiveState = passiveValue;
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            if (items != null) {
                boolean foundOne = false;

                for (Item item : items) {
                    if (activeState.equals(item.getStateAs(activeState.getClass()))) {
                        if (foundOne) {
                            return passiveState;
                        } else {
                            foundOne = true;
                        }
                    }
                }
                if (foundOne) {
                    return activeState;
                }
            }

            return passiveState;
        }

        @Override
        public @Nullable <T extends State> T getStateAs(@Nullable Set<Item> items, Class<T> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return stateClass.cast(state);
            } else {
                if (stateClass == DecimalType.class) {
                    if (items != null) {
                        return stateClass.cast(new DecimalType(count(items, activeState)));
                    } else {
                        return stateClass.cast(DecimalType.ZERO);
                    }
                } else {
                    return null;
                }
            }
        }

        private int count(Set<Item> items, State state) {
            int count = 0;
            for (Item item : items) {
                if (state.equals(item.getStateAs(state.getClass()))) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public State[] getParameters() {
            return new State[] { activeState, passiveState };
        }
    }

    /**
     * This calculates the numeric average over all item states of decimal type.
     */
    class Avg implements GroupFunction {

        public Avg() {
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;
            if (items != null) {
                for (Item item : items) {
                    DecimalType itemState = item.getStateAs(DecimalType.class);
                    if (itemState != null) {
                        sum = sum.add(itemState.toBigDecimal());
                        count++;
                    }
                }
            }
            if (count > 0) {
                return new DecimalType(sum.divide(BigDecimal.valueOf(count), MathContext.DECIMAL128));
            } else {
                return UnDefType.UNDEF;
            }
        }

        @Override
        public @Nullable <T extends State> T getStateAs(@Nullable Set<Item> items, Class<T> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return stateClass.cast(state);
            } else {
                return null;
            }
        }

        @Override
        public State[] getParameters() {
            return new State[0];
        }
    }

    /**
     * This calculates the numeric median over all item states of decimal type.
     */
    class Median implements GroupFunction {

        public Median() {
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            if (items != null) {
                List<BigDecimal> states = items.stream().map(item -> item.getStateAs(DecimalType.class))
                        .filter(Objects::nonNull).map(DecimalType::toBigDecimal).toList();
                BigDecimal median = Statistics.median(states);
                if (median != null) {
                    return new DecimalType(median);
                }
            }
            return UnDefType.UNDEF;
        }

        @Override
        public @Nullable <T extends State> T getStateAs(@Nullable Set<Item> items, Class<T> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return stateClass.cast(state);
            } else {
                return null;
            }
        }

        @Override
        public State[] getParameters() {
            return new State[0];
        }
    }

    /**
     * This calculates the numeric sum over all item states of decimal type.
     */
    class Sum implements GroupFunction {

        public Sum() {
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            BigDecimal sum = BigDecimal.ZERO;
            if (items != null) {
                for (Item item : items) {
                    DecimalType itemState = item.getStateAs(DecimalType.class);
                    if (itemState != null) {
                        sum = sum.add(itemState.toBigDecimal());
                    }
                }
            }
            return new DecimalType(sum);
        }

        @Override
        public @Nullable <T extends State> T getStateAs(@Nullable Set<Item> items, Class<T> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return stateClass.cast(state);
            } else {
                return null;
            }
        }

        @Override
        public State[] getParameters() {
            return new State[0];
        }
    }

    /**
     * This calculates the minimum value of all item states of decimal type.
     */
    class Min implements GroupFunction {

        public Min() {
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            if (items != null && !items.isEmpty()) {
                BigDecimal min = null;
                for (Item item : items) {
                    DecimalType itemState = item.getStateAs(DecimalType.class);
                    if (itemState != null) {
                        if (min == null || min.compareTo(itemState.toBigDecimal()) > 0) {
                            min = itemState.toBigDecimal();
                        }
                    }
                }
                if (min != null) {
                    return new DecimalType(min);
                }
            }
            return UnDefType.UNDEF;
        }

        @Override
        public @Nullable <T extends State> T getStateAs(@Nullable Set<Item> items, Class<T> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return stateClass.cast(state);
            } else {
                return null;
            }
        }

        @Override
        public State[] getParameters() {
            return new State[0];
        }
    }

    /**
     * This calculates the maximum value of all item states of decimal type.
     */
    class Max implements GroupFunction {

        public Max() {
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            if (items != null && !items.isEmpty()) {
                BigDecimal max = null;
                for (Item item : items) {
                    DecimalType itemState = item.getStateAs(DecimalType.class);
                    if (itemState != null) {
                        if (max == null || max.compareTo(itemState.toBigDecimal()) < 0) {
                            max = itemState.toBigDecimal();
                        }
                    }
                }
                if (max != null) {
                    return new DecimalType(max);
                }
            }
            return UnDefType.UNDEF;
        }

        @Override
        public @Nullable <T extends State> T getStateAs(@Nullable Set<Item> items, Class<T> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return stateClass.cast(state);
            } else {
                return null;
            }
        }

        @Override
        public State[] getParameters() {
            return new State[0];
        }
    }

    /**
     * This calculates the number of items in the group matching the
     * regular expression passed in parameter
     * Group:Number:COUNT(".") will count all items having a string state of one character
     * Group:Number:COUNT("[5-9]") will count all items having a string state between 5 and 9
     * ...
     */
    class Count implements GroupFunction {

        protected final Pattern pattern;

        public Count(@Nullable State regExpr) {
            if (regExpr == null) {
                throw new IllegalArgumentException("Parameter must not be null!");
            }
            this.pattern = Pattern.compile(regExpr.toString());
        }

        @Override
        public State calculate(@Nullable Set<Item> items) {
            int count = 0;
            if (items != null) {
                for (Item item : items) {
                    Matcher matcher = pattern.matcher(item.getState().toString());
                    if (matcher.matches()) {
                        count++;
                    }
                }
            }
            return new DecimalType(count);
        }

        @Override
        public @Nullable <T extends State> T getStateAs(@Nullable Set<Item> items, Class<T> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return stateClass.cast(state);
            } else {
                return null;
            }
        }

        @Override
        public State[] getParameters() {
            return new State[] { new StringType(pattern.pattern()) };
        }
    }
}
