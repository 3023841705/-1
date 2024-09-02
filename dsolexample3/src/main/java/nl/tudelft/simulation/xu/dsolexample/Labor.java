package nl.tudelft.simulation.xu.dsolexample;

import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.AtomicModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.CoupledModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.InputPort;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.OutputPort;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.Phase;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

public class Labor extends AtomicModel
{
	private static final long serialVersionUID = 6544848791484470266L;

	/** input port */
	public InputPort<String> in = new InputPort<String>(this);
	/** output port */
	public OutputPort<String> loading = new OutputPort<String>(this);
	/** output port */
	public OutputPort<String> request_ship = new OutputPort<String>(this);
	/** output port */
	public OutputPort<String> request_truck = new OutputPort<String>(this);
	/** output port */
	public OutputPort<String> out = new OutputPort<String>(this);

	/** time (minutes) needed to drill for a truck */
	private DistContinuous loadingTimeDistribution;

	/** possible phases of the miner */
	/** request to load a truck */
	private final Phase HAVE_REQUEST = new Phase("have_request");
	/** load a truck */
	private final Phase Loading = new Phase("loading");
	/** a transient phase to send request immediately */
	private final Phase TRANSIENT_PHASE = new Phase("transient_phase");

	/** print debug information or not */
	private boolean printInfo = false;
	/** state variables */
	private Phase current_status;
	private String serving_truck;

	public Labor(String modelName, CoupledModel parentModel)
	{
		super(modelName, parentModel);
		StreamInterface stream = new Java2Random();
		this.loadingTimeDistribution = new DistUniform(stream, 40.0, 50.0);
		// this.drillingTimeDistribution = new DistConstant(stream, 20.0);
		setPhase(TRANSIENT_PHASE, 0.0);
		printDebugMessage("function: constructor(); initial state: " + this.current_status);
	}

	@Override
	protected void deltaInternal()
	{
		if (this.current_status.equals(TRANSIENT_PHASE))
		{
			setPhase(HAVE_REQUEST, Double.POSITIVE_INFINITY);
			printDebugMessage("function: deltaInternal(); transition: " + TRANSIENT_PHASE + "-->" + this.current_status);
		} else if (this.current_status.equals(Loading))
		{
			this.serving_truck = "";
			setPhase(TRANSIENT_PHASE, 0.0);
			printDebugMessage("function: deltaInternal(); transition: " + Loading + "-->" + this.current_status);
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
				this.serving_truck = (String) value;
				double duration = this.loadingTimeDistribution.draw();
				setPhase(Loading, duration);
				printDebugMessage("function: deltaExternal(" + e + "," + value + "); reaction: " + HAVE_REQUEST + "-->"
						+ this.current_status + "[" + this.serving_truck + "; " + duration + "].");
			}
		}
	}

	@Override
	protected void lambda()
	{
		if (this.current_status.equals(TRANSIENT_PHASE))
		{
			this.request_truck.send(getModelName());
			printDebugMessage("function: lambda(); phase: " + this.current_status
					+ "; message: request an empty truck at warehouse.");
		}
		if (this.current_status.equals(Loading))
		{
			this.loading.send(this.serving_truck);
			this.request_ship.send("back to dockA.");
			this.out.send("Load_Finish");
			printDebugMessage("function: lambda(); phase: " + this.current_status + "; message: finish load "
					+ this.serving_truck + "; ship sent back to dockA.");
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
