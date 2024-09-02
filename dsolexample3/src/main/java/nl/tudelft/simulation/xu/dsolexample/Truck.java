package nl.tudelft.simulation.xu.dsolexample;

import java.io.File;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.AtomicModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.CoupledModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.InputPort;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.OutputPort;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.Phase;

public class Truck extends AtomicModel
{
	private static final long serialVersionUID = -7898939009021151232L;

	/** input ports and output ports */
	public InputPort<String> load_finish = new InputPort<String>(this);
	public InputPort<String> unload_finish = new InputPort<String>(this);
	public OutputPort<String> out_to_labor = new OutputPort<String>(this);
	public OutputPort<String> out_to_ship = new OutputPort<String>(this);
	public OutputPort<String> out = new OutputPort<String>(this);

	/** traveling time (min) of the truck when full */
	private final double timeTravelWhenFull = 20;
	/** traveling time (min) of the truck when empty */
	private final double timeTravelWhenEmpty = 15;
	
	/** write waiting time into txt */
	private FileWrite fw;
	//private String filename1 = "C:/Users/ASUS/Desktop/data/initial/rou/waiting_at_warehouseA.txt";
	private String filename2 = "C:/Users/ASUS/Desktop/data/initial/rou/waiting_at_dockA.txt";

	/** possible phases of the truck */
	/** truck travel to miner (empty) */
	private final Phase TRAVEL_TO_WAREHOUSE = new Phase("travel_to_warehouse");
	/** truck travel to elevator (with ore) */
	private final Phase TRAVEL_TO_DOCK = new Phase("travel_to_dock");
	/** wait to be served */
	private final Phase WAITING = new Phase("waiting");
	/** a transient phase to notify arrival in the beginning */
	private final Phase TRANSIENT_PHASE = new Phase("transient_phase");

	/** print debug information or not */
	private boolean printInfo = false;
	/** state variables */
	private Phase current_status;

	public Truck(String modelName, CoupledModel parentModel)
	{
		super(modelName, parentModel);
		setPhase(TRANSIENT_PHASE, 0.0);
		printDebugMessage("function: constructor(); initial state: " + this.current_status);

	}

	@Override
	protected void deltaInternal()
	{
		if (this.current_status.equals(TRANSIENT_PHASE) || this.current_status.equals(TRAVEL_TO_DOCK)
				|| this.current_status.equals(TRAVEL_TO_WAREHOUSE))
		{
			printDebugMessage("function: deltaInternal(); transition: " + this.current_status + "-->" + WAITING);
			setPhase(WAITING, Double.POSITIVE_INFINITY);
		}
	}

	@SuppressWarnings("static-access")
	@Override
	protected void deltaExternal(double e, Object value)
	{
		if (this.activePort.equals(load_finish) && ((String) value).equals(getModelName()))
		{
			if (!this.current_status.equals(WAITING))
			{
				System.err.println(getModelName() + ": not allowed!");
			} else
			{
				setPhase(TRAVEL_TO_DOCK, timeTravelWhenFull);
				printDebugMessage("function: deltaExternal(" + e + "," + value + ")[load finish]; reaction: " + WAITING
						+ "-->" + this.current_status + "[" + timeTravelWhenFull + "].");
				//fw.WriteObjectDouble(this.filename1, e, value);
				/*try {
					fw.WriteDoubleDouble(filename1, e, getSimulator().getSimulatorTime().get());
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
			}
		}
		if (this.activePort.equals(unload_finish) && ((String) value).equals(getModelName()))
		{
			if (!this.current_status.equals(WAITING))
			{
				System.err.println(getModelName() + ": not allowed!");
			} else
			{
				setPhase(TRAVEL_TO_WAREHOUSE, timeTravelWhenEmpty);
				printDebugMessage("function: deltaExternal(" + e + "," + value + ")[unload finish]; reaction: "
						+ WAITING + "-->" + this.current_status + "[" + timeTravelWhenEmpty + "].");
				//fw.WriteObjectDouble(this.filename2, e, value);
				try {
					fw.WriteDoubleDouble(filename2, e, getSimulator().getSimulatorTime().get());
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void lambda()
	{
		if (this.current_status.equals(TRANSIENT_PHASE) || this.current_status.equals(TRAVEL_TO_WAREHOUSE))
		{
			out_to_labor.send(getModelName());
			this.out.send("Truck_Arrived_Warehouse");
			printDebugMessage("function: lambda(); phase: " + this.current_status + "; message: arrive at warehouse.");
		}
		if (this.current_status.equals(TRAVEL_TO_DOCK))
		{
			out_to_ship.send(getModelName());
			this.out.send("Truck_Arrived_DockA");
			printDebugMessage("function: lambda(); phase: " + this.current_status + "; message: arrive at dockA.");
		}
	}

	@Override
	protected double timeAdvance()
	{
		return this.current_status.getLifeTime();
	}

	public void setPhase(Phase phase, double lifeTime)
	{
		this.current_status = phase;
		this.current_status.setLifeTime(lifeTime);
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
