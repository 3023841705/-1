package nl.tudelft.simulation.xu.dsolexample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileWrite
{
	public static void WriteObjectDouble(String filename, double data, Object value)
	{
		try
		{
			File dataFile = new File(filename);
			dataFile.getParentFile().mkdirs();
			if (!dataFile.exists())
			{
				dataFile.createNewFile();
			}
			
			FileWriter fW = new FileWriter(dataFile.getAbsoluteFile(), true);
			BufferedWriter bW = new BufferedWriter(fW);
			
			bW.write(value + "  " + data + "\r\n");
			
			/* if Array
			int i = 0;
			for (double t : data)
			{
				i ++;
				if (i < data.length)
				{
					bW.write(t + "\r\n");
				} 
				else
				{
					bW.write(t + "");
				}
			}
			*/
			
			bW.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void WriteDoubleDouble(String filename, double data, double time)
	{
		try
		{
			File dataFile = new File(filename);
			dataFile.getParentFile().mkdirs();
			if (!dataFile.exists())
			{
				dataFile.createNewFile();
			}
			
			FileWriter fW = new FileWriter(dataFile.getAbsoluteFile(), true);
			BufferedWriter bW = new BufferedWriter(fW);
			
			bW.write(time + "  " + data + "\r\n");
			
			/* if Array
			int i = 0;
			for (double t : data)
			{
				i ++;
				if (i < data.length)
				{
					bW.write(t + "\r\n");
				} 
				else
				{
					bW.write(t + "");
				}
			}
			*/
			
			bW.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
