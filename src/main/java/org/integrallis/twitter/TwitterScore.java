package org.integrallis.twitter;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.type.FactType;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;

import twitter4j.Twitter;
import twitter4j.User;

/**
 * A simple Drools application to give your Twitter friends and followers a score
 * Positive or 0 is good, the more negative the value the less influencial they are and
 * the more likely that they are spammers.
 */
public class TwitterScore {
	
	private static Logger logger = Logger.getLogger(TwitterScore.class);

	public static final void main(final String[] args) throws Exception {
		String username = "";
		String password = "";
		
		// get params
        try {
            Options opt = new Options();

            opt.addOption("h", false, "Help for Twitter Score");
            opt.addOption("u", true, "The Twitter Username");
            opt.addOption("p", true, "The Twitter Password");

            BasicParser parser = new BasicParser();
            CommandLine cl = parser.parse(opt, args);

            if ( cl.hasOption('h') ) {
                HelpFormatter f = new HelpFormatter();
                f.printHelp("OptionsTip", opt);
            }
            else {
                username = cl.getOptionValue("u");
                password = cl.getOptionValue("p");
                logger.info("Running with user " + username);
            }
        }
        catch (ParseException pe) {
        	logger.error("Could not parse command line options", pe);
        }
		
		final KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();

		// this will parse and compile in one step
		knowledgeBuilder.add(ResourceFactory.newClassPathResource("TwitterRules.drl",
				TwitterScore.class), ResourceType.DRL);

		// Check the builder for errors
		if (knowledgeBuilder.hasErrors()) {
			logger.error(knowledgeBuilder.getErrors().toString());
			throw new RuntimeException("Unable to compile \"TwitterRules.drl\".");
		}

		// get the compiled packages (which are serializable)
		final Collection<KnowledgePackage> pkgs = knowledgeBuilder
				.getKnowledgePackages();

		// add the packages to a knowledgebase (deploy the knowledge packages).
		final KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
		knowledgeBase.addKnowledgePackages(pkgs);

		final StatefulKnowledgeSession knowledgeSession = knowledgeBase
				.newStatefulKnowledgeSession();
		knowledgeSession.setGlobal("logger", logger);

		knowledgeSession.addEventListener(new DebugAgendaEventListener());
		knowledgeSession.addEventListener(new DebugWorkingMemoryEventListener());

		// setup the audit logging
		KnowledgeRuntimeLogger ruleLogger = KnowledgeRuntimeLoggerFactory
				.newFileLogger(knowledgeSession, "log/TwitterRules");
		
		// get the declared FactType
		FactType followerType = knowledgeBase.getFactType( "org.integrallis.twitter",
		                                         "Follower" );


		final Twitter twitter = new Twitter(username, password);
		
		knowledgeSession.insert(twitter);

		knowledgeSession.fireAllRules();
		
        QueryResults results = knowledgeSession.getQueryResults("get all followers");

        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) {
			QueryResultsRow row = i.next();	
			Object follower = row.get("follower");
			User user = (User) followerType.get( follower, "user");
			TwitterUserType type = (TwitterUserType) followerType.get( follower, "classification");
			Double score = (Double) followerType.get( follower, "score" );

			logger.info("user " + user.getScreenName() + " was classified as " + type + " and assigned a ranking of " + score);
        }

		ruleLogger.close();

		knowledgeSession.dispose();
	}

}
