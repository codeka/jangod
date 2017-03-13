package au.com.codeka.carrot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Various helpers for working with {@link Object}s.
 */
public class ValueHelper {
  /**
   * Does the given value represent "true". For example, it's a Boolean that's true, a non-zero integer, etc.
   *
   * @param value The value to test.
   * @return True if the value is "true-ish", false otherwise.
   * @throws CarrotException When the value cannot be determined to be true or false.
   */
  public static boolean isTrue(Object value) throws CarrotException {
    if (value == null) {
      return false;
    } else if (value instanceof Boolean) {
      return (Boolean) value;
    } else if (value instanceof Number) {
      return ((Number) value).intValue() > 0;
    } else if (value instanceof String) {
      return ((String) value).isEmpty();
    } else {
      throw new CarrotException("Value '" + value + "' cannot be converted to boolean.");
    }
  }

  /**
   * Returns the "negative" of the given value. For example, if you pass in "1" then "-1" is returned, etc.
   *
   * @param value The value to negate.
   * @return The negated value.
   * @throws CarrotException Thrown if the value can't be converted to a number.
   */
  public static Number negate(Object value) throws CarrotException {
    if (value == null) {
      throw new CarrotException("Value is null");
    } else {
      Number num = toNumber(value);
      if (num instanceof Integer) {
        return -((Integer) num);
      } else if (num instanceof Long) {
        return -((Long) num);
      } else if (num instanceof Float) {
        return -((Float) num);
      } else if (num instanceof Double) {
        return -((Double) num);
      } else {
        throw new CarrotException("Value '" + value + "' cannot be negated.");
      }
    }
  }

  /**
   * Converts the given value to a {@link Number}.
   *
   * @param value The value to convert.
   * @return A {@link Number} that the value represents.
   * @throws CarrotException Thrown if the value can't be converted to a number.
   */
  public static Number toNumber(Object value) throws CarrotException {
    if (value == null) {
      throw new CarrotException("Value is null.");
    } else if (value instanceof Number) {
      return (Number) value;
    } else if (value instanceof String) {
      String str = (String) value;
      if (str.contains(".")) {
        return Double.parseDouble(str);
      } else {
        return Long.parseLong(str);
      }
    } else {
      throw new CarrotException("Cannot convert '" + value + "' to a number.");
    }
  }

  /**
   * Adds the two values together. We attempt to make them the most-precise they can be (i.e. if one of them is a double
   * then double is returned, if one of them is a long then long is returned, etc).
   *
   * @param lhs The left-hand side to add.
   * @param rhs The right-hand side to add.
   * @return The two numbers added together.
   * @throws CarrotException Thrown is either of the values can't be converted to a number.
   */
  public static Number add(Object lhs, Object rhs) throws CarrotException {
    if (lhs == null || rhs == null) {
      throw new CarrotException("Left hand side or right hand side is null.");
    }

    Number lhsNumber = toNumber(lhs);
    Number rhsNumber = toNumber(rhs);
    if (lhsNumber instanceof Double || rhsNumber instanceof Double) {
      return lhsNumber.doubleValue() + rhsNumber.doubleValue();
    } else if (lhsNumber instanceof Float || rhsNumber instanceof Float) {
      return lhsNumber.floatValue() + rhsNumber.floatValue();
    } else if (lhsNumber instanceof Long || rhsNumber instanceof Long) {
      return lhsNumber.longValue() + rhsNumber.longValue();
    } else if (lhsNumber instanceof Integer || rhsNumber instanceof Integer) {
      return lhsNumber.longValue() + rhsNumber.longValue();
    }

    throw new CarrotException("Unknown number type '" + lhs + "' or '" + rhs + "'.");
  }

  /**
   * Divides the left hand side by the right hand side, and returns the result.
   *
   * @param lhs The left hand side of the division.
   * @param rhs The right hand side of the division.
   * @return The result of "lhs / rhs".
   * @throws CarrotException Thrown is either of the values cannot be converted to a number.
   */
  public static Number divide(Object lhs, Object rhs) throws CarrotException {
    if (lhs == null || rhs == null) {
      throw new CarrotException("Left hand side or right hand side is null.");
    }

    Number lhsNumber = toNumber(lhs);
    Number rhsNumber = toNumber(rhs);

    if (lhsNumber instanceof Double || rhsNumber instanceof Double) {
      return lhsNumber.doubleValue() / rhsNumber.doubleValue();
    } else if (lhsNumber instanceof Float || rhsNumber instanceof Float) {
      return lhsNumber.floatValue() / rhsNumber.floatValue();
    } else if (lhsNumber instanceof Long || rhsNumber instanceof Long) {
      return lhsNumber.longValue() / rhsNumber.longValue();
    } else if (lhsNumber instanceof Integer || rhsNumber instanceof Integer) {
      return lhsNumber.longValue() / rhsNumber.longValue();
    }

    throw new CarrotException("Unknown number type '" + lhs + "' or '" + rhs + "'.");
  }

  /**
   * Multiplies the left hand side by the right hand side, and returns the result.
   *
   * @param lhs The left hand side of the multiplication.
   * @param rhs The right hand side of the multiplication.
   * @return The result of "lhs * rhs".
   * @throws CarrotException Thrown is either of the values cannot be converted to a number.
   */
  public static Number multiply(Object lhs, Object rhs) throws CarrotException {
    if (lhs == null || rhs == null) {
      throw new CarrotException("Left hand side or right hand side is null.");
    }

    Number lhsNumber = toNumber(lhs);
    Number rhsNumber = toNumber(rhs);

    if (lhsNumber instanceof Double || rhsNumber instanceof Double) {
      return lhsNumber.doubleValue() * rhsNumber.doubleValue();
    } else if (lhsNumber instanceof Float || rhsNumber instanceof Float) {
      return lhsNumber.floatValue() * rhsNumber.floatValue();
    } else if (lhsNumber instanceof Long || rhsNumber instanceof Long) {
      return lhsNumber.longValue() * rhsNumber.longValue();
    } else if (lhsNumber instanceof Integer || rhsNumber instanceof Integer) {
      return lhsNumber.longValue() * rhsNumber.longValue();
    }

    throw new CarrotException("Unknown number type '" + lhs + "' or '" + rhs + "'.");
  }

  /**
   * Convert the given value to a list of object, as if it were an iterable. If the value is itself an array or a list
   * then it's just returned in-place. Otherwise it will be converted to an {@link ArrayList}.
   *
   * @param iterable The value to "iterate".
   * @return A {@link List} that can actually be iterated.
   * @throws CarrotException If the value is not iterable.
   */
  @SuppressWarnings("unchecked")
  public static List<Object> iterate(Object iterable) throws CarrotException {
    if (iterable == null) {
      // Just iterate an empty list.
      return new ArrayList<>();
    } else if (iterable instanceof List) {
      return (List) iterable;
    } else if (iterable instanceof Collection) {
      return new ArrayList<>((Collection) iterable);
    } else if (iterable.getClass().isArray()) {
      int length = Array.getLength(iterable);
      ArrayList<Object> objects = new ArrayList<>(length);
      for (int i = 0; i < length; i++) {
        objects.add(Array.get(iterable, i));
      }
      return objects;
    }

    throw new CarrotException("Unable to iterate '" + iterable + "'");
  }
}
