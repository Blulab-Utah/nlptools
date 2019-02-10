package edu.utah.bmi.nlp.simple.gui.tasks;

import edu.utah.bmi.nlp.core.GUITask;
import edu.utah.bmi.nlp.ctakes.wrapper.ClinicPipe;
import edu.utah.bmi.simple.gui.entry.TasksFX;

public class RefreshClinicPipe extends GUITask {
	protected GUITask guiTask;
	private TasksFX tasks;
	private static ClinicPipe clinicPipe;

	public RefreshClinicPipe(TasksFX tasks) {
		this.tasks = tasks;
		guiTask = this;
	}

	@Override
	protected Object call() throws Exception {
		if (guiEnabled) {
			clinicPipe = ClinicPipe.getInstance(tasks);
			clinicPipe.setGuiTask(this);
			clinicPipe.refreshPipe();
		}


		return null;
	}

	public static void showResults() {
		if (clinicPipe != null)
			clinicPipe.showResults();
	}
}
