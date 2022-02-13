package data;

/**
 * 개요 : <br>
 * 작성일 : Aug 22, 2012<br>
 * 작성자 : 민경현<br>
 * Version : 1.00
 */
public class SgProperties{
	
	private String key;
	private String value;
	
	public SgProperties() {}
	
	/**
	 * 생성자
	 * @param key
	 * @param value
	 */
	public SgProperties(String key, String value)
	{
		this.key = key;
		this.value = value;
	}
	
	/**
	 * @return
	 */
	public String getKey()
	{
		return key;
	}

	public String getStringValue()
	{
		return value;
	}
	
	public Boolean getBooleanValue()
	{
		return Boolean.valueOf(value);
	}
	
	public Integer getIntegerValue()
	{
		return Integer.valueOf(value);
	}
	
	public Double getDoubleValue()
	{
		return Double.valueOf(value);
	}
	
	public Float getFloatValue()
	{
		return Float.valueOf(value);
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public void setValue(String key, String value)
	{
		this.key = key;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "seegene.data.SjProperties[key="+key+", value="+value+"]";
	}
}

