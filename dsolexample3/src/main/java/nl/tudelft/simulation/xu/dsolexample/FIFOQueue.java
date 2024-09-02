package nl.tudelft.simulation.xu.dsolexample;

import java.util.LinkedList;
import java.util.Queue;

import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.AtomicModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.CoupledModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.InputPort;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.OutputPort;

public class FIFOQueue extends AtomicModel
{
	private static final long serialVersionUID = 34233876973972276L;

	/** input ports and output ports */
	public InputPort<String> arrival = new InputPort<String>(this);
	public InputPort<String> request = new InputPort<String>(this);
	public OutputPort<String> beReady = new OutputPort<String>(this);
	public OutputPort<String> out = new OutputPort<String>(this);

	/** 'customer' list: c1,c2,... */
	private Queue<String> customers = new LinkedList<String>();
	/** 'request' list: s1,s2,... */
	private Queue<String> requests = new LinkedList<String>();

	/** print debug information or not */
	private boolean printInfo = false;

	public FIFOQueue(String modelName, CoupledModel parentModel)
	{
		super(modelName, parentModel);
		this.sigma = Double.POSITIVE_INFINITY;
		printDebugMessage("function: constructor(); initial state: customers = " + this.customers
				+ "; requests = " + this.requests);
	}

	@Override
	protected void deltaInternal()
	{
		if (this.customers.size() == 0 || this.requests.size() == 0)
		{
			System.err.println(getModelName() + ": internal transition impossible!!!");
		} else
		{
			this.customers.poll();
			this.requests.poll();
			if (this.requests.size() > 0 && this.customers.size() > 0)
			{
				this.sigma = 0.0;
			} else
			{
				this.sigma = Double.POSITIVE_INFINITY;
			}
			String str = "{customers = " + this.customers + "; requests = " + this.requests + "}";
			printDebugMessage("function: deltaInternal(); transition: " + str);
		}
	}

	@Override
	protected void deltaExternal(double e, Object value)
	{
		if (this.activePort.equals(request))
		{
			this.requests.add((String) value);
			if (this.requests.size() == 1 && this.customers.size() > 0)
			{
				this.sigma = 0.0;
			} else if (this.customers.size() == 0)
			{
				this.sigma = Double.POSITIVE_INFINITY;
			}
			String str = "{customers = " + this.customers + "; requests = " + this.requests + "}";
			printDebugMessage("function: deltaExternal(" + e + "," + value + "): " + this.requests.peek()
					+ " is available {" + str + "}.");
		}
		if (this.activePort.equals(arrival))
		{
			this.customers.add((String) value);

			if (this.customers.size() == 1 && this.requests.size() > 0)
			{
				this.sigma = 0.0;

			} else if (this.requests.size() == 0)
			{
				this.sigma = Double.POSITIVE_INFINITY;
			}
			String str = "{customers = " + this.customers + "; requests = " + this.requests + "}";
			printDebugMessage("function: deltaExternal(" + e + "," + value + "): " + this.customers.peek()
					+ " arrives {" + str + "}.");
		}
	}

	@Override
	protected void lambda()
	{
		this.beReady.send(this.customers.peek());
		this.out.send(this.requests.peek() + "_serve_" + this.customers.peek());
		printDebugMessage("function: lambda(); message: " + this.requests.peek() + " serve " + this.customers.peek());
	}

	@Override
	protected double timeAdvance()
	{
		return this.sigma;
	}

	public void printDebugMessage(String message)
	{
		try
		{
			if (printInfo)
			{
				double now = getSimulator().getSimulatorTime().get();
				System.out.println(getModelName() + "[" + now + "]: " + message);
			}
		} catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public final void setPrintInfo(boolean printInfo)
	{
		this.printInfo = printInfo;
	}
}
