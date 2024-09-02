package nl.tudelft.simulation.xu.dsolexample;

import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.AtomicModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.CoupledModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.InputPort;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.OutputPort;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.Phase;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

public class Ship extends AtomicModel
{
	private static final long serialVersionUID = 1667340013462952201L;

	/** input ports and output ports */
	public InputPort<String> in = new InputPort<String>(this);
	public InputPort<String> back_to_docka = new InputPort<String>(this);
	public InputPort<String> load = new InputPort<String>(this);
	public OutputPort<String> unload_truck = new OutputPort<String>(this);
	public OutputPort<String> load_truck = new OutputPort<String>(this);
	public OutputPort<String> request_truck_a = new OutputPort<String>(this);
	public OutputPort<String> request_truck_b = new OutputPort<String>(this);
	public OutputPort<String> out = new OutputPort<String>(this);

	/** time (minutes) needed to go to dock B with ore */
	private final double timeToBWithOre = 45;
	/** time (minutes) needed to go to dock A empty */
	private final double timeToAEmpty = 30;
	/** time (minutes) needed to unload the truck at dock A */
	private DistContinuous unloadTruckAtDockATimeDistribution;
	/** time (minutes) needed to unload the ore at dock B */
	private DistContinuous loadTruckAtDockBTimeDistribution;

	/** possible phases of the ship */
	/** request to unload a truck at dock A */
	private final Phase HAVE_REQUEST = new Phase("have_request");
	private final Phase HAVE_REQUEST_B = new Phase("have_request_b");
	/** go to dock B with ore */
	private final Phase GO_TO_DOCKB_WITH_ORE = new Phase("go_to_dockb_with_ore");
	/** go to dock A empty */
	private final Phase GO_BACK_TO_DOCKA_EMPTY = new Phase("go_back_to_docka_empty");
	/** unload truck at dock A */
	private final Phase UNLOAD_TRUCK_AT_DOCKA = new Phase("unload_truck_at_docka");
	/** unload ore at dock B */
	private final Phase LOAD_TRUCK_AT_DOCKB = new Phase("load_truck_at_dockb");
	/** idle at dock B (after unloading ore at belt) */
	private final Phase IDLE_AT_DOCKB = new Phase("idle_at_dockb");
	/** a transient phase to send request immediately */
	private final Phase TRANSIENT_PHASE = new Phase("transient_phase");
	private final Phase TRANSIENT_PHASE_B = new Phase("transient_phase_b");
	/** ship carrying capacity */

	/** print debug information or not */
	private boolean printInfo = true;
	/** current phase of the elevator */
	private Phase current_status;
	private String unloading_truck;
	private String Loading_truck;
	private boolean hasUnprocessedRequest = false;

	public Ship(String modelName, CoupledModel parentModel)
	{
		super(modelName, parentModel);

		/** distributions */
		StreamInterface stream1 = new Java2Random();
		this.unloadTruckAtDockATimeDistribution = new DistUniform(stream1, 15.0, 20.0);
		// this.unloadTruckAtBottomTimeDistribution = new DistConstant(stream1, 5.0);
		StreamInterface stream2 = new Java2Random();
		this.loadTruckAtDockBTimeDistribution = new DistUniform(stream2, 15.0, 20.0);
		// this.unloadOreAtTopTimeDistribution = new DistConstant(stream2, 2.0);

		/** force sending a request */
		setPhase(TRANSIENT_PHASE, 0.0);
		printDebugMessage("function: constructor(); initial state: " + this.current_status);
	}

	@Override
	protected void deltaInternal()
	{
		/** phase transitions */
		if (this.current_status.equals(TRANSIENT_PHASE))
		{
			setPhase(HAVE_REQUEST, Double.POSITIVE_INFINITY);
			printDebugMessage("function: deltaInternal(); transition: " + TRANSIENT_PHASE + "-->" + this.current_status);
		} else if (this.current_status.equals(TRANSIENT_PHASE_B))
		{
			setPhase(HAVE_REQUEST_B, Double.POSITIVE_INFINITY);
			printDebugMessage("function: deltaInternal(); transition: " + TRANSIENT_PHASE_B + "-->" + this.current_status);
		} else if (this.current_status.equals(UNLOAD_TRUCK_AT_DOCKA))
		{
			this.unloading_truck = "";
			setPhase(GO_TO_DOCKB_WITH_ORE, timeToBWithOre);
			printDebugMessage("function: deltaInternal(); transition: " + UNLOAD_TRUCK_AT_DOCKA + "-->" + this.current_status);
		} else if (this.current_status.equals(GO_TO_DOCKB_WITH_ORE))
		{
			setPhase(TRANSIENT_PHASE_B, 0.0);
			printDebugMessage("function: deltaInternal(); transition: " + GO_TO_DOCKB_WITH_ORE + "-->" + this.current_status);
		} else if (this.current_status.equals(LOAD_TRUCK_AT_DOCKB))
		{
			this.Loading_truck = "";
			if (this.hasUnprocessedRequest)
			{
				this.hasUnprocessedRequest = false;
				setPhase(GO_BACK_TO_DOCKA_EMPTY, timeToAEmpty);
			} else
			{
				setPhase(IDLE_AT_DOCKB, Double.POSITIVE_INFINITY);
			}
			printDebugMessage("function: deltaInternal(); transition: " + LOAD_TRUCK_AT_DOCKB + "-->" + this.current_status);
		} else if (this.current_status.equals(GO_BACK_TO_DOCKA_EMPTY))
		{
			setPhase(TRANSIENT_PHASE, 0.0);
			printDebugMessage("function: deltaInternal(); transition: " + GO_BACK_TO_DOCKA_EMPTY + "-->" + this.current_status);
		}
	}

	@Override
	protected void deltaExternal(double e, Object value)
	{
		if (this.activePort.equals(in))
		{
			if (!this.current_status.equals(HAVE_REQUEST))
			{
				System.err.println(getModelName() + ": not allowed!");
			} else
			{
				this.unloading_truck = (String) value;
				double duration = unloadTruckAtDockATimeDistribution.draw();
				setPhase(UNLOAD_TRUCK_AT_DOCKA, duration);
				printDebugMessage("function: deltaExternal(" + e + "," + value + "); reaction: " + HAVE_REQUEST + "-->"
						+ this.current_status + "[" + this.unloading_truck + "; " + duration + "].");
			}
		}
		if (this.activePort.equals(load))
		{
			if (!this.current_status.equals(HAVE_REQUEST_B))
			{
				System.err.println(getModelName() + ": not allowed!");
			} else
			{
				this.Loading_truck = (String) value;
				double duration = loadTruckAtDockBTimeDistribution.draw();
				setPhase(LOAD_TRUCK_AT_DOCKB, duration);
				printDebugMessage("function: deltaExternal(" + e + "," + value + "); reaction: " + HAVE_REQUEST_B +  "-->"
						+ this.current_status + "[" + this.Loading_truck + ";" + duration + "].");
			}
		}
		if (this.activePort.equals(back_to_docka))
		{
			if (this.current_status.equals(IDLE_AT_DOCKB))
			{
				setPhase(GO_BACK_TO_DOCKA_EMPTY, timeToAEmpty);
				printDebugMessage("function: deltaExternal(" + e + "," + value + "); reaction: " + IDLE_AT_DOCKB + "-->"
						+ this.current_status);

			} else if (this.current_status.equals(UNLOAD_TRUCK_AT_DOCKA) || this.current_status.equals(GO_TO_DOCKB_WITH_ORE)
					|| this.current_status.equals(LOAD_TRUCK_AT_DOCKB))
			{
				this.hasUnprocessedRequest = true;
				double newLifeTime = this.current_status.getLifeTime() - e;
				this.current_status.setLifeTime(newLifeTime);
				printDebugMessage("function: deltaExternal(" + e + "," + value + "); reaction: " + this.current_status
						+ " [no immediate reaction.]");
			}
		}
	}

	@Override
	protected void lambda()
	{
		if (this.current_status.equals(TRANSIENT_PHASE))
		{
			this.request_truck_a.send(getModelName());
			this.out.send("Ship_Arrived_DockA");
			printDebugMessage("function: lambda(); phase: " + this.current_status
					+ "; message: request a full truck at dockA.");
		}
		if (this.current_status.equals(TRANSIENT_PHASE_B))
		{
			this.request_truck_b .send(getModelName());
			this.out.send("Ship_Arrived_DockB");
			printDebugMessage("function: lambda(); phase: " + this.current_status
					+ "; message: request an empty truck at dockB.");
		}
		if (this.current_status.equals(GO_TO_DOCKB_WITH_ORE))
		{
			this.out.send("Ship_Arrived_DockB");
		}
		if (this.current_status.equals(GO_BACK_TO_DOCKA_EMPTY))
		{

		}
		if (this.current_status.equals(UNLOAD_TRUCK_AT_DOCKA))
		{
			this.unload_truck.send(unloading_truck);
			this.out.send("Unload_Finish");
			printDebugMessage("function: lambda(); phase: " + this.current_status + "; message: " + "unload "
					+ this.unloading_truck + " finish.");
		}
		if (this.current_status.equals(LOAD_TRUCK_AT_DOCKB))
		{

			this.load_truck.send(Loading_truck);
			this.out.send("Load_Finish");
			printDebugMessage("function: lambda(); phase: " + this.current_status + "; message: load" + this.Loading_truck + "finish");
		}
	}

	@Override
	protected double timeAdvance()
	{
		return this.current_status.getLifeTime();
	}

	private void setPhase(Phase phase, double lifeTime)
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
