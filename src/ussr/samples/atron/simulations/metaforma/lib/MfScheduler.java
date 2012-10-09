package ussr.samples.atron.simulations.metaforma.lib;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;

public class MfScheduler {
	private MfController ctrl;
	public Map <String,Float> intervals = new HashMap<String,Float>();
	public Map <String,Float> previousAction = new HashMap<String,Float>();
	
	public MfScheduler (MfController c) {
		ctrl = c;
	}
	
	public void setInterval (String name, float interval) {
		intervals.put(name, interval);
	}
	
		
	public boolean isScheduled (String func) {
		if (!previousAction.containsKey(func)) {
			previousAction.put(func, 0f);
		}
		
		if (!intervals.containsKey(func)) {
			try {
				throw new Exception("Task " + func + " does not exists!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (ctrl.time()-previousAction.get(func) > intervals.get(func)) {
			scheduleNext(func);
			return true;
		}
		return false;
			
	}
	
	
	public void scheduleNext (String name) {
		previousAction.put(name, ctrl.time());
//		previousAction.put(Operation.DISCOVER, ctrl.time()); // Discover is used when other messages are not send for some time
	}

	public void invokeIfScheduled(String func) {
		//ctrl.notification(".invokeIfScheduled(" + func + ")");
		if (isScheduled(func)) {
			invoke (func);
		}
		
	}

	private void invoke(String func) {
		Method m;
		Object params[] = {};
		Object dest = null;
		if (func.startsWith("meta.")) {
			dest = ctrl.meta();
			func = func.replace("meta.", "");
		}
		else if (func.startsWith("module.")) {
			dest = ctrl.module();
			func = func.replace("module.", "");
		}
		else {
			dest = ctrl;
		}
		
		try {
			m = dest.getClass().getMethod(func);
			m.setAccessible(true);
			
			m.invoke(dest,params);
		}
		catch(NoSuchMethodException e) {
			System.out.println(e.getMessage());
		}
		catch (Exception e) {
			System.err.println("Error on invoking " + func);
			e.printStackTrace();
		}
	}

	public void invokeNow(String func) {
//		ctrl.visual.print(".invokeNow(" + func + ")");
		previousAction.put(func, ctrl.time());
		invoke(func);
	}

	public void sync() {
		for (String f:intervals.keySet()) {
			invokeIfScheduled(f);
		}
		
	}

	public void invokeNowDiscover() {
		invokeNow("module.discover");
		
	}

	public void enable(String action) {
		if (!intervals.containsKey(action)) {
			ctrl.visual.print("ENABLE " + action);
			intervals.put(action, ctrl.getSettings().getInterval(action));
		}
	}
	
	public void disable(String action) {
		if (intervals.containsKey(action)) {
			ctrl.visual.print("DISABLE " + action);
			intervals.remove(action);
		}
	}

	public void invokeNowConsensus() {
		invokeNow("module.broadcastConsensus");
		
	}

}
