package cn.zpl.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 时间处理通用类
 * 
 * @version 1.0
 * @author 
 * @since java 1.6
 */
public class DateHandler {

  public static int openDay = 5;

  private String iDate = "";

  private int iYear;

  private int iMonth;

  private int iDay;

  private int iHour;

  private int iMiunte;

  private static DateFormat ddFormat = new SimpleDateFormat("yyyy-MM-dd");

  private static DateFormat ddChFormat = new SimpleDateFormat("yyyy年M月d日");

  private static DateFormat ddShortFormat = new SimpleDateFormat("yyyyMMdd");

  private static DateFormat ssFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static DateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmssSSSS");

  /**
   * @param date
   * @return yyyy-MM-dd
   */
  public static String dateToDdString(Date date) {
    if (date == null) {
      return "";
    }
    return ddFormat.format(date);
  }

  /**
   * @return String yyyy年M月d日
   */
  public static String dateToDdChString(Date date) {
    if (date == null) {
      return null;
    }
    return ddChFormat.format(date);
  }

  public static String dateToShortDdString(Date date) {
    return ddShortFormat.format(date);
  }

  public synchronized static String getCurrentDateNo() {
    try {
      Thread.sleep(1);
    } catch (Exception e) {
    }
    Date date = new Date();
    date.setTime(System.currentTimeMillis());
    return df2.format(date);
  }

  public static Date getCurDate() {
    return ddStringToDate(dateToDdString(new Date()));
  }
  /**
   * 
   * @param date
   * @return yyyy-MM-dd HH:mm:ss
   */
  public static String dateToSsString(Date date) {
    if (date != null) {
      return ssFormat.format(date);
    } else {
      return "";
    }
  }
  /**
   * 
   * @param dateStr
   *            yyyy-MM-dd
   * @return
   */
  public static Date ddStringToDate(String dateStr) {
    Date date = null;
    try {
      date = ddFormat.parse(dateStr);
    } catch (ParseException pe) {
      return null;
    }
    return date;
  }

  public DateHandler() {
  }
  public DateHandler(String str) {
    iDate = str;
  }

  public void setDate(String iDateTime) {
    this.iDate = iDateTime.substring(0, 10);
  }

  public String getDate() {
    return this.iDate;
  }

  public int getYear() {
    iYear = Integer.parseInt(iDate.substring(0, 4));
    return iYear;
  }

  public int getMonth() {
    iMonth = Integer.parseInt(iDate.substring(5, 7));
    return iMonth;
  }

  public int getDay() {
    iDay = Integer.parseInt(iDate.substring(8, 10));
    return iDay;
  }

  public int getHour() {
    if (iDate.length() == 16) {
      iHour = Integer.parseInt(iDate.substring(11, 13));
    } else {
      iHour = 0;
    }
    return iHour;
  }

  public int getMinute() {
    if (iDate.length() == 16) {
      iMiunte = Integer.parseInt(iDate.substring(14, 16));
    } else {
      iMiunte = 0;
    }
    return iMiunte;
  }

  public static String subDate(String date) {
    return date.substring(0, 10);
  }

  /**
   * 根据输入日期，计算是否是季度末
   * 
   * @param date
   *            日期
   * @return 是否季度末
   */
  public static boolean isSeason(String date) {
    int getMonth = Integer.parseInt(date.substring(5, 7));
    boolean sign = false;
    if (getMonth == 3) {
      sign = true;
    }
    if (getMonth == 6) {
      sign = true;
    }
    if (getMonth == 9) {
      sign = true;
    }
    if (getMonth == 12) {
      sign = true;
    }
    return sign;
  }

  /**
   * 计算从现在开始几天后的时间
   * 
   * @param afterDay
   *            天数
   * @return 从现在开始afterDay天后的时间
   * @author cc
   */
  public static String getDateFromNow(int afterDay) {
    GregorianCalendar calendar = new GregorianCalendar();
    Date date = calendar.getTime();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + afterDay);
    date = calendar.getTime();
    return df.format(date);
  }

  /**
   * 计算从现在开始几天后的时间(带格式)
   * 
   * @param afterDay
   *            天数
   * @param format_string
   *            格式
   * @return 从现在开始afterDay天后的时间
   * @author tim
   */
  public static String getDateFromNow(int afterDay, String format_string) {
    Calendar calendar = Calendar.getInstance();
    Date date = null;
    DateFormat df = new SimpleDateFormat(format_string);
    calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + afterDay);
    date = calendar.getTime();
    return df.format(date);
  }

  /**
   * 得到当前时间，用于文件名，没有特殊字符，使用yyyyMMddHHmmss格式
   * 
   * @param afterDay
   *            天数
   * @return 当前时间
   * @author tim
   */
  public static String getNowForFileName(int afterDay) {
    GregorianCalendar calendar = new GregorianCalendar();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + afterDay);
    Date date = calendar.getTime();
    return df.format(date);
  }

  /**
   * 比较日期，与N天后日期的比较
   * 
   * @param limitDate
   *            日期字符串，保存当前时间
   * @param afterDay
   *            天数
   * @return 比较的结果
   */
  public int getDateCompare(String limitDate, int afterDay) {
    GregorianCalendar calendar = new GregorianCalendar();
    Date date = calendar.getTime();
    calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + afterDay);
    date = calendar.getTime(); // date是新来的天数，跟今天相比的天数
    iDate = limitDate;
    calendar.set(getYear(), getMonth() - 1, getDay());
    Date dateLimit = calendar.getTime();
    return dateLimit.compareTo(date);
  }

  /**
   * 比较日期，与现在的日期对比
   * 
   * @param limitDate
   *            日期字符串，保存当前时间
   * @return 比较的结果
   */
  public int getDateCompare(String limitDate) {
    iDate = limitDate;
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.set(getYear(), getMonth() - 1, getDay());
    Date date = calendar.getTime();
    Date nowDate = new Date();
    return date.compareTo(nowDate);
  }

  /**
   * 比较日期+时间，与现在的日期对比
   * 
   * @param limitDate
   *            日期字符串，保存当前时间
   * @return 比较的结果
   */
  public int getDateTimeCompare(String limitDateTime) {
    iDate = limitDateTime;
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.set(getYear(), getMonth() - 1, getDay(), getHour(), getMinute());
    Date date = calendar.getTime();
    Date nowDate = new Date();
    return date.compareTo(nowDate);
  }

  /**
   * 比较日期，与现在的日期对比
   * 
   * @param limitDate
   *            日期字符串，保存当前时间
   * @return 相差的天数
   */
  public long getLongCompare(String limitDate) {
    iDate = limitDate;
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.set(getYear(), getMonth() - 1, getDay());
    Date date = calendar.getTime();
    long datePP = date.getTime();
    Date nowDate = new Date();
    long dateNow = nowDate.getTime();
    return ((dateNow - datePP) / (24 * 60 * 60 * 1000));
  }

  /**
   * 比较日期（日期参数1<日期参数2）
   * 
   * @param limitDate1
   *            日期字符串1
   * @param limitDate2
   *            日期字符串2
   * @return 相差的天数
   */
  public long getLongCompare(String limitDate1, String limitDate2) {
    iDate = limitDate1;
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.set(getYear(), getMonth() - 1, getDay());
    Date date = calendar.getTime();
    long datePP = date.getTime();
    iDate = limitDate2;
    if (limitDate2 == null || limitDate2.trim().equals("")) {
      iDate = getToday();
    }
    calendar.set(getYear(), getMonth() - 1, getDay());
    Date date2 = calendar.getTime();
    long dateNow = date2.getTime();
    return ((dateNow - datePP) / (24 * 60 * 60 * 1000));
  }

  /**
   * 获取今天的日期的字符串
   * 
   * @return 今天的日期的字符串
   */
  public static String getToday() {
    Calendar cld = Calendar.getInstance();
    Date date = new Date();
    cld.setTime(date);
    int intMon = cld.get(Calendar.MONTH) + 1;
    int intDay = cld.get(Calendar.DAY_OF_MONTH);
    String mons = String.valueOf(intMon);
    String days = String.valueOf(intDay);
    if (intMon < 10) {
      mons = "0" + String.valueOf(intMon);
    }
    if (intDay < 10) {
      days = "0" + String.valueOf(intDay);
    }
    return String.valueOf(cld.get(Calendar.YEAR)) + "-" + mons + "-" + days;
  }

  /**
   * 获取当前年份
   * 
   * @return 年度信息
   */
  public static int getCurrentYear() {
    Calendar cld = Calendar.getInstance();
    Date date = new Date();
    cld.setTime(date);
    return cld.get(Calendar.YEAR);
  }

  /**
   * 获取昨天的日期的字符串
   * 
   * @return 昨天的日期的字符串
   */
  public static String getYestoday() {
    Calendar cld = Calendar.getInstance();
    Date date = new Date();
    cld.setTime(date);
    cld.add(Calendar.DATE, -1);
    int intMon = cld.get(Calendar.MONTH) + 1;
    int intDay = cld.get(Calendar.DAY_OF_MONTH);
    String mons = String.valueOf(intMon);
    String days = String.valueOf(intDay);
    if (intMon < 10) {
      mons = "0" + String.valueOf(intMon);
    }
    if (intDay < 10) {
      days = "0" + String.valueOf(intDay);
    }
    return String.valueOf(cld.get(Calendar.YEAR)) + "-" + mons + "-" + days;
  }

  /**
   * 计算员工的工作天数 此函数用来计算员工的工作天数，如在使用期和离职期该月份的工作日 输入（离职日期，-1）可得该月工作天数
   * 时间以2002-12-14为准 输入（入司时间，1）可的该月工作天数
   * 
   * @param date
   *            日期
   * @param sign
   *            天数
   * @return 工作天数
   */
  public static int getWorkDay(String date, int sign) {
    int month = 0;
    int week = 0;
    int workDay = 0;
    Calendar rightNow = Calendar.getInstance();

    DateHandler dateOver = new DateHandler();
    dateOver.setDate(date);

    rightNow.set(Calendar.YEAR, dateOver.getYear());
    rightNow.set(Calendar.MONTH, dateOver.getMonth() - 1);
    rightNow.set(Calendar.DATE, dateOver.getDay());

    month = rightNow.get(Calendar.MONTH);

    while (rightNow.get(Calendar.MONTH) == month) {
      week = rightNow.get(Calendar.DAY_OF_WEEK);
      if (week == 1 || week == 7) {
      } else {
        workDay++;
      }
      rightNow.add(Calendar.DATE, sign);
    }
    return workDay;
  }

  /**
   * 得到当前月份
   * 
   * @return 当前月份
   */
  public static int getCurrentMonth() {
    GregorianCalendar dt = new GregorianCalendar();
    return dt.get(GregorianCalendar.MONTH) + 1;
  }

  /**
   * 获取给定日期和当前日期之间的工作日（给定日期小于当前日期）
   * 
   * @param limitDate
   * @return 天数
   */
  public int getWorkDay(String limitDate) {
    int lday = (int) getLongCompare(limitDate);
    Calendar rightNow = Calendar.getInstance();
    DateHandler dateOver = new DateHandler();
    dateOver.setDate(getToday());
    rightNow.set(Calendar.YEAR, dateOver.getYear());
    rightNow.set(Calendar.MONTH, dateOver.getMonth() - 1);
    rightNow.set(Calendar.DATE, dateOver.getDay());
    int week = rightNow.get(Calendar.WEEK_OF_YEAR);// 当前周
    Calendar limDate = Calendar.getInstance();
    dateOver.setDate(limitDate);
    limDate.set(Calendar.YEAR, dateOver.getYear());
    limDate.set(Calendar.MONTH, dateOver.getMonth() - 1);
    limDate.set(Calendar.DATE, dateOver.getDay());
    int limit_week = limDate.get(Calendar.WEEK_OF_YEAR);// 给定周
    int iday = 0;
    int iweek = rightNow.get(Calendar.DAY_OF_WEEK);
    if (iweek == 7)// Saturday
    {
      iday++;
    }
    if (iweek == 1)// Sunday
    {
      iday = 2;
    }
    int limitday = 0;
    int limitweek = limDate.get(Calendar.DAY_OF_WEEK);
    if (limitweek == 7) // Saturday
    {
      limitday++;
    }
    if (limitweek == 1) // Sunday
    {
      limitday = 2;
    }
    int irestDay = (week - limit_week) * 2 + iday - limitday;
    return lday - irestDay;
  }

  /**
   * 获取给定两个日期之间的工作日（日期参数1<日期参数2）
   * 
   * @param limitDate
   * @return 天数
   */
  public int getWorkDay(String limitDate1, String limitDate2) {
    if (limitDate2 == null || limitDate2.trim().equals("")) {
      limitDate2 = getToday();
    }
    int lday = (int) getLongCompare(limitDate1, limitDate2);
    DateHandler dateOver = new DateHandler();

    Calendar limDate2 = Calendar.getInstance();
    dateOver.setDate(limitDate2);
    limDate2.set(Calendar.YEAR, dateOver.getYear());
    limDate2.set(Calendar.MONTH, dateOver.getMonth() - 1);
    limDate2.set(Calendar.DATE, dateOver.getDay());
    int week = limDate2.get(Calendar.WEEK_OF_YEAR); // 给定周2

    Calendar limDate1 = Calendar.getInstance();
    dateOver.setDate(limitDate1);
    limDate1.set(Calendar.YEAR, dateOver.getYear());
    limDate1.set(Calendar.MONTH, dateOver.getMonth() - 1);
    limDate1.set(Calendar.DATE, dateOver.getDay());
    int limit_week = limDate1.get(Calendar.WEEK_OF_YEAR); // 给定周1

    int iday = 0;
    int iweek = limDate2.get(Calendar.DAY_OF_WEEK);
    if (iweek == 7) // Saturday
    {
      iday++;
    }
    if (iweek == 1) // Sunday
    {
      iday = 2;
    }

    int limitday = 0;
    int limitweek = limDate1.get(Calendar.DAY_OF_WEEK);
    if (limitweek == 7) // Saturday
    {
      limitday++;
    }
    if (limitweek == 1) // Sunday
    {
      limitday = 2;
    }
    int irestDay = (week - limit_week) * 2 + iday - limitday;
    return lday - irestDay;
  }

  /**
   * 格式化当前时间成最后版本所需要的时间格式
   * 
   * @return 格式YYYY-MM-DD HH:MM:SS
   */
  public static String getLastVerTime() {
    GregorianCalendar calendar = new GregorianCalendar();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return df.format(calendar.getTime());
  }

  public static void main(String[] args) {
    DateHandler h = new DateHandler();
    //System.out.println(h.getWorkDay("2006-06-10"));
  }

	/**
	 * 日期相加减
	 * 
	 * @param time
	 *            时间字符串 yyyy-MM-dd HH:mm:ss
	 * @param num
	 *            加的数，-num就是减去
	 * @return 减去相应的数量的年的日期
	 * @throws ParseException
	 */
	public static Date yearAddNum(Date time, Integer num) {
		// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date date = format.parse(time);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		calendar.add(Calendar.YEAR, num);
		Date newTime = calendar.getTime();
		return newTime;
	}
	/**
	 * 
	 * @param time
	 *            时间
	 * @param num
	 *            加的数，-num就是减去
	 * @return 减去相应的数量的月份的日期
	 * @throws ParseException
	 *             Date
	 */
	public static Date monthAddNum(Date time, Integer num) {
		// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date date = format.parse(time);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		calendar.add(Calendar.MONTH, num);
		Date newTime = calendar.getTime();
		return newTime;
	}

	/**
	 * 
	 * @param time
	 *            时间
	 * @param num
	 *            加的数，-num就是减去
	 * @return 减去相应的数量的天的日期
	 * @throws ParseException
	 *             Date
	 */
	public static Date dayAddNum(Date time, Integer num) {
		// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date date = format.parse(time);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		calendar.add(Calendar.DAY_OF_MONTH, num);
		Date newTime = calendar.getTime();
		return newTime;
	} 
  
	public static Date strToDate(String strDate, String sFormat) throws ParseException {
		DateFormat format = new SimpleDateFormat(sFormat);
		return format.parse(strDate);
	}

	public static String dateToStr(Date date, String sFormat) {
		SimpleDateFormat df = new SimpleDateFormat(sFormat);
		return df.format(date);
	}
	
/*	3.Date转long型
	Date date = new Date();
	long dateTime = date.getTime();
	4.long型转Date
	long dateTime = 14830682769461;
	Date date = new Date(dateTime);*/

}
