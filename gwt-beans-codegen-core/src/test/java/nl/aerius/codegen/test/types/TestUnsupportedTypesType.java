package nl.aerius.codegen.test.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Test class for types that are not supported in the parser generator.
 * This class contains fields with types that are intentionally not supported.
 */
public class TestUnsupportedTypesType {
  // Java 8+ Types
  private Optional<String> optionalString;
  private LocalDate localDate;
  private LocalDateTime localDateTime;

  // Complex Number Types
  private BigDecimal bigDecimal;
  private BigInteger bigInteger;

  // Special Types
  private UUID uuid;
  private Date date;
  private Calendar calendar;

  // Collection Limitations
  private SortedSet<String> sortedSet;
  private TreeSet<Integer> treeSet;
  private SortedMap<String, Integer> sortedMap;
  private TreeMap<String, Double> treeMap;

  // Getters and setters
  public Optional<String> getOptionalString() {
    return optionalString;
  }

  public void setOptionalString(Optional<String> optionalString) {
    this.optionalString = optionalString;
  }

  public LocalDate getLocalDate() {
    return localDate;
  }

  public void setLocalDate(LocalDate localDate) {
    this.localDate = localDate;
  }

  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }

  public void setLocalDateTime(LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }

  public BigDecimal getBigDecimal() {
    return bigDecimal;
  }

  public void setBigDecimal(BigDecimal bigDecimal) {
    this.bigDecimal = bigDecimal;
  }

  public BigInteger getBigInteger() {
    return bigInteger;
  }

  public void setBigInteger(BigInteger bigInteger) {
    this.bigInteger = bigInteger;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Calendar getCalendar() {
    return calendar;
  }

  public void setCalendar(Calendar calendar) {
    this.calendar = calendar;
  }

  public SortedSet<String> getSortedSet() {
    return sortedSet;
  }

  public void setSortedSet(SortedSet<String> sortedSet) {
    this.sortedSet = sortedSet;
  }

  public TreeSet<Integer> getTreeSet() {
    return treeSet;
  }

  public void setTreeSet(TreeSet<Integer> treeSet) {
    this.treeSet = treeSet;
  }

  public SortedMap<String, Integer> getSortedMap() {
    return sortedMap;
  }

  public void setSortedMap(SortedMap<String, Integer> sortedMap) {
    this.sortedMap = sortedMap;
  }

  public TreeMap<String, Double> getTreeMap() {
    return treeMap;
  }

  public void setTreeMap(TreeMap<String, Double> treeMap) {
    this.treeMap = treeMap;
  }

  /**
   * Creates a fully populated instance with test values.
   */
  public static TestUnsupportedTypesType createFullObject() {
    TestUnsupportedTypesType obj = new TestUnsupportedTypesType();
    obj.setOptionalString(Optional.of("test"));
    obj.setLocalDate(LocalDate.of(2024, 3, 1));
    obj.setLocalDateTime(LocalDateTime.of(2024, 3, 1, 12, 0));
    obj.setBigDecimal(new BigDecimal("123.456"));
    obj.setBigInteger(BigInteger.valueOf(123456));
    obj.setUuid(UUID.randomUUID());
    obj.setDate(new Date());
    obj.setCalendar(Calendar.getInstance());
    obj.setSortedSet(new TreeSet<>());
    obj.setTreeSet(new TreeSet<>());
    obj.setSortedMap(new TreeMap<>());
    obj.setTreeMap(new TreeMap<>());
    return obj;
  }

  /**
   * Creates an instance with null values.
   */
  public static TestUnsupportedTypesType createNullObject() {
    TestUnsupportedTypesType obj = new TestUnsupportedTypesType();
    obj.setOptionalString(null);
    obj.setLocalDate(null);
    obj.setLocalDateTime(null);
    obj.setBigDecimal(null);
    obj.setBigInteger(null);
    obj.setUuid(null);
    obj.setDate(null);
    obj.setCalendar(null);
    obj.setSortedSet(null);
    obj.setTreeSet(null);
    obj.setSortedMap(null);
    obj.setTreeMap(null);
    return obj;
  }
}