package edu.utah.bmi.nlp.simple.gui.tasks;

import edu.utah.bmi.nlp.core.GUITask;
import edu.utah.bmi.nlp.ctakes.wrapper.ClinicPipe;
import edu.utah.bmi.nlp.easycie.writer.SQLWriterCasConsumer;
import edu.utah.bmi.simple.gui.controller.TasksOverviewController;
import edu.utah.bmi.simple.gui.entry.TasksFX;
import javafx.application.Platform;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

/**
 * @author Jianlin Shi on 6/17/18.
 */
public class RunClinicPipe extends GUITask {
	protected ClinicPipe fastDebugPipe;
	protected TasksFX tasks;
	protected String option;

	public RunClinicPipe(TasksFX tasks) {
		initiate(tasks, "db");
	}

	public RunClinicPipe(TasksFX tasks, String option) {
		initiate(tasks, option);
	}

	private void initiate(TasksFX tasks, String option) {
		if (!Platform.isFxApplicationThread()) {
			guiEnabled = false;
		}
		if (TasksOverviewController.currentTasksOverviewController.currentGUITask == null) {
			TasksOverviewController.currentTasksOverviewController.currentGUITask = this;
		}
		this.tasks = tasks;
		this.option = option;
	}

	@Override
	protected Object call() throws Exception {
		if (guiEnabled) {
			fastDebugPipe = ClinicPipe.getInstance(tasks, option);
			fastDebugPipe.setGuiTask(this);

//			update run_id, because run() will not sync the run_id from uimaLogger by itself.
			AnalysisEngineDescription writerDescriptor = fastDebugPipe.runner.getAEDesriptors().get(fastDebugPipe.runner.getAEDesriptors().size() - 1);
			if (writerDescriptor.getMetaData().getName().toLowerCase().indexOf("writer") != -1) {
				if (writerDescriptor.getMetaData().getConfigurationParameterSettings().getParameterValue(SQLWriterCasConsumer.PARAM_VERSION) != null) {
					Object run_id = fastDebugPipe.getUimaLogger().getRunid();
					writerDescriptor.getMetaData().getConfigurationParameterSettings().setParameterValue(SQLWriterCasConsumer.PARAM_VERSION, run_id+ "");
				}
			}
			fastDebugPipe.run();
		}
		return null;
	}
}
