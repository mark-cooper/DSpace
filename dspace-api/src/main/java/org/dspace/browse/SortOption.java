/*
 * SortOption.java
 *
 * Copyright (c) 2002-2007, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.dspace.browse;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.core.ConfigurationManager;

/**
 * Class to mediate with the sort configuration
 * 
 * @author Richard Jones
 *
 */
public class SortOption
{
	/** the sort configuration number */
	private int number;
	
	/** the name of the sort */
	private String name;
	
	/** the metadata field to sort on */
	private String metadata;
	
	/** the type of data we are sorting by */
	private String type;
	
	/** the metadata broken down into bits for convenience */
	private String[] mdBits;

    /** the sort options available for this index */
    private static Set<SortOption> sortOptionsSet = null;
    private static Map<Integer, SortOption> sortOptionsMap = null;
	
	/**
	 * Construct a new SortOption object with the given parameters
	 * 
	 * @param number
	 * @param name
	 * @param md
	 * @param type
	 * @throws BrowseException
	 */
	public SortOption(int number, String name, String md, String type)
		throws BrowseException
	{
		this.name = name;
		this.type = type;
		this.metadata = md;
		this.number = number;
		generateMdBits();
	}

	/** 
	 * Construct a new SortOption object using the definition from the configuration
	 * 
	 * @param number
	 * @param definition
	 * @throws BrowseException
	 */
	public SortOption(int number, String definition)
		throws BrowseException
	{
		this.number = number;
		
		String rx = "(\\w+):([\\w\\.\\*]+):(\\w+)";
        Pattern pattern = Pattern.compile(rx);
        Matcher matcher = pattern.matcher(definition);
        
        if (!matcher.matches())
        {
            throw new BrowseException("Sort Order configuration is not valid: webui.browse.sort-order." + 
                    number + " = " + definition);
        }
        
        name = matcher.group(1);
        metadata = matcher.group(2);
        type = matcher.group(3);
        generateMdBits();
	}
	
	/**
	 * @return Returns the metadata.
	 */
	public String getMetadata()
	{
		return metadata;
	}

	/**
	 * @param metadata The metadata to set.
	 */
	public void setMetadata(String metadata)
	{
		this.metadata = metadata;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return Returns the number.
	 */
	public int getNumber()
	{
		return number;
	}

	/**
	 * @param number The number to set.
	 */
	public void setNumber(int number)
	{
		this.number = number;
	}
	
	/**
	 * @return	a 3 element array of the metadata bits
	 */
	public String[] getMdBits()
    {
    	return mdBits;
    }
    
	/**
	 * Tell the class to generate the metadata bits
	 * 
	 * @throws BrowseException
	 */
    public void generateMdBits()
    	throws BrowseException
    {
    	try
    	{
    		mdBits = interpretField(metadata, null);
    	}
    	catch(IOException e)
    	{
    		throw new BrowseException(e);
    	}
    }
    
    /**
     * Take a string representation of a metadata field, and return it as an array.
     * This is just a convenient utility method to basically break the metadata 
     * representation up by its delimiter (.), and stick it in an array, inserting
     * the value of the init parameter when there is no metadata field part.
     * 
     * @param mfield	the string representation of the metadata
     * @param init	the default value of the array elements
     * @return	a three element array with schema, element and qualifier respectively
     */
    public String[] interpretField(String mfield, String init)
    	throws IOException
    {
    	StringTokenizer sta = new StringTokenizer(mfield, ".");
    	String[] field = {init, init, init};
    	
    	int i = 0;
    	while (sta.hasMoreTokens())
    	{
    		field[i++] = sta.nextToken();
    	}
    	
    	// error checks to make sure we have at least a schema and qualifier for both
    	if (field[0] == null || field[1] == null)
    	{
    		throw new IOException("at least a schema and element be " +
    				"specified in configuration.  You supplied: " + mfield);
    	}
    	
    	return field;
    }
    
    /**
     * Is this a date field
     * 
     * @return
     */
    public boolean isDate()
    {
    	if (type != null)
    	{
    		if ("date".equals(type))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Is the default sort option
     * 
     * @return
     */
    public boolean isDefault()
    {
    	if (number == 0)
    	{
    		return true;
    	}
    	return false;
    }

    /**
     * @return	a map of the configured sort options
     */
    public static Map<Integer, SortOption> getSortOptionsMap() throws BrowseException
    {
        if (SortOption.sortOptionsMap != null)
            return SortOption.sortOptionsMap;
        
        SortOption.sortOptionsMap = new HashMap<Integer, SortOption>();
        synchronized (SortOption.sortOptionsMap)
        {
            for (SortOption so : SortOption.getSortOptions())
            {
                SortOption.sortOptionsMap.put(new Integer(so.getNumber()), so);
            }
        }
        
    	return SortOption.sortOptionsMap;
    }

    /**
     * Return all the configured sort options
     * @return
     * @throws BrowseException
     */
    public static Set<SortOption> getSortOptions() throws BrowseException
    {
        if (SortOption.sortOptionsSet != null)
            return SortOption.sortOptionsSet;
        
        SortOption.sortOptionsSet = new HashSet<SortOption>();
        synchronized (SortOption.sortOptionsSet)
        {
            Enumeration en = ConfigurationManager.propertyNames();
            
            String rx = "webui\\.browse\\.sort-option\\.(\\d+)";
            Pattern pattern = Pattern.compile(rx);
            
            while (en.hasMoreElements())
            {
                String property = (String) en.nextElement();
                Matcher matcher = pattern.matcher(property);
                if (matcher.matches())
                {
                    int number = Integer.parseInt(matcher.group(1));
                    String option = ConfigurationManager.getProperty(property);
                    SortOption so = new SortOption(number, option);
                    SortOption.sortOptionsSet.add(so);
                }
            }
        }

        return SortOption.sortOptionsSet;
    }
    
    /**
     * Get the defined sort option by number (.1, .2, etc)
     * @param number
     * @return
     * @throws BrowseException
     */
    public static SortOption getSortOption(int number) throws BrowseException
    {
        for (SortOption so : SortOption.getSortOptions())
        {
            if (so.getNumber() == number)
                return so;
        }
        
        return null;
    }
    
    /**
     * Get the default sort option - initially, just the first one defined
     * @return
     * @throws BrowseException
     */
    public static SortOption getDefaultSortOption() throws BrowseException
    {
        for (SortOption so : getSortOptions())
            return so;
        
        return null;
    }
}
