package data.stack;

import java.util.Arrays;
import java.util.EmptyStackException;

public class SgStack <T> 
{
	private T[] elements;
	private static final int DEFAULT_SIZE  = 16;
	int size = 0;
	
	@SuppressWarnings("unchecked")
	public SgStack() 
	{
		elements = (T[]) new Object[DEFAULT_SIZE];
	}
	
	public void push(T e)
	{
		ensureCapacity();
		elements[size++] = e;
	}
	
	public T pop()
	{
		if(elements.length == 0)
		{
			throw new EmptyStackException();
		}
		T e = elements[size];
		elements[--size] = null;
		
		return e;
	}
	
	private void ensureCapacity()
	{
		if(elements.length == size)
		{
			elements = Arrays.copyOf(elements, 2 * elements.length +1);
		}
	}	
}
