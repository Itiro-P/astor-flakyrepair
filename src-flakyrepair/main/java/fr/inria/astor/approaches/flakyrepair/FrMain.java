package fr.inria.astor.approaches.flakyrepair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import fr.inria.astor.approaches.cardumen.CardumenApproach;
import fr.inria.astor.approaches.deeprepair.DeepRepairEngine;
import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.approaches.jgenprog.extension.TibraApproach;
import fr.inria.astor.approaches.jkali.JKaliEngine;
import fr.inria.astor.approaches.jmutrepair.jMutRepairExhaustive;
import fr.inria.astor.approaches.scaffold.ScaffoldRepairEngine;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.ingredientbased.ExhaustiveIngredientBasedEngine;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.main.AbstractMain;
import fr.inria.main.ExecutionMode;
import fr.inria.main.ExecutionResult;
import fr.inria.main.evolution.AstorMain;
import fr.inria.main.evolution.ExtensionPoints;

public class FrMain extends AstorMain {

	protected Logger log = Logger.getLogger(AstorMain.class.getName());

	protected AstorCoreEngine core = null;

	@Override
	public ExecutionResult run(String location, String projectName, String dependencies, String packageToInstrument,
			double thfl, String failing) throws Exception {

		long startT = System.currentTimeMillis();
		initProject(location, projectName, dependencies, packageToInstrument, thfl, failing);

		String mode = ConfigurationProperties.getProperty("mode").toLowerCase();
		String customEngine = ConfigurationProperties.getProperty(ExtensionPoints.NAVIGATION_ENGINE.identifier);

		if (customEngine != null && !customEngine.isEmpty())
			core = createEngine(ExecutionMode.custom);
		else {
			for (ExecutionMode executionMode : ExecutionMode.values()) {
				for (String acceptedName : executionMode.getAcceptedNames()) {
					if (acceptedName.equals(mode)) {
						core = createEngine(executionMode);
						break;
					}
				}
			}

			if (core == null) {
				System.err.println("Unknown mode of execution: '" + mode + "',  modes are: "
						+ Arrays.toString(ExecutionMode.values()));
				return null;
			}

		}

		ConfigurationProperties.print();

		core.startSearch();

		ExecutionResult result = core.atEnd();

		long endT = System.currentTimeMillis();
		log.info("Time Total(s): " + (endT - startT) / 1000d);

		return result;
	}

	@Override
	public ExecutionResult execute(String[] args) throws Exception {
		boolean correct = processArguments(args);

		log.info("Running Astor on a JDK at " + System.getProperty("java.home"));

		if (!correct) {
			System.err.println("Problems with commands arguments");
			return null;
		}
		if (isExample(args)) {
			executeExample(args);
			return null;
		}

		String dependencies = ConfigurationProperties.getProperty("dependenciespath");
		dependencies += (ConfigurationProperties.hasProperty("extendeddependencies"))
				? (File.pathSeparator + ConfigurationProperties.hasProperty("extendeddependencies"))
				: "";
		String failing = ConfigurationProperties.getProperty("failing");
		String location = ConfigurationProperties.getProperty("location");
		String packageToInstrument = ConfigurationProperties.getProperty("packageToInstrument");
		double thfl = ConfigurationProperties.getPropertyDouble("flthreshold");
		String projectName = ConfigurationProperties.getProperty("projectIdentifier");

		setupLogging();

		ExecutionResult result = run(location, projectName, dependencies, packageToInstrument, thfl, failing);
		return result;
	}
}
