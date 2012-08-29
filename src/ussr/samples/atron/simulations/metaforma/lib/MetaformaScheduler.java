package ussr.samples.atron.simulations.metaforma.lib;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;

public class MetaformaScheduler {
	private MetaformaController ctrl;
	public Map <String,Integer> intervalMs = new HashMap<String,Integer>();
	public Map <String,Float> previousAction = new HashMap<String,Float>();
	
	public MetaformaScheduler (MetaformaController c) {
		ctrl = c;
	}
	
	public void setInterval (String name, int interval) {
		intervalMs.put(name, interval);
	}
	
		
	public boolean isScheduled (String func) {
		if (!previousAction.containsKey(func)) {
			previousAction.put(func, 0f);
		}
		
		if (!intervalMs.containsKey(func)) {
			try {
				throw new Exception("Task " + func + " does not exists!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (ctrl.time()-previousAction.get(func) > intervalMs.get(func)/1000) {
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
		try {
//			System.out.println("Invoking " + func);
//			System.out.println(ctrl.getClass().getSuperclass().getSuperclass());
			m = ctrl.getClass().getMethod(func);
			m.invoke(ctrl,params);
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
		for (String f:intervalMs.keySet()) {
			invokeIfScheduled(f);
		}
		
	}
}
