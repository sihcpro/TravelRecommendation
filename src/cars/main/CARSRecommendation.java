package cars.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Table;

import carskit.alg.baseline.avg.ContextAverage;
import carskit.alg.baseline.avg.GlobalAverage;
import carskit.alg.baseline.avg.ItemAverage;
import carskit.alg.baseline.avg.ItemContextAverage;
import carskit.alg.baseline.avg.UserAverage;
import carskit.alg.baseline.avg.UserContextAverage;
import carskit.alg.baseline.avg.UserItemAverage;
import carskit.alg.baseline.cf.BPMF;
import carskit.alg.baseline.cf.BiasedMF;
import carskit.alg.baseline.cf.ItemKNN;
import carskit.alg.baseline.cf.NMF;
import carskit.alg.baseline.cf.PMF;
import carskit.alg.baseline.cf.SVDPlusPlus;
import carskit.alg.baseline.cf.SlopeOne;
import carskit.alg.baseline.cf.UserKNN;
import carskit.alg.baseline.ranking.BPR;
import carskit.alg.baseline.ranking.LRMF;
import carskit.alg.baseline.ranking.RankALS;
import carskit.alg.baseline.ranking.RankSGD;
import carskit.alg.baseline.ranking.SLIM;
import carskit.alg.cars.adaptation.dependent.FM;
import carskit.alg.cars.adaptation.dependent.dev.CAMF_C;
import carskit.alg.cars.adaptation.dependent.dev.CAMF_CI;
import carskit.alg.cars.adaptation.dependent.dev.CAMF_CU;
import carskit.alg.cars.adaptation.dependent.dev.CAMF_CUCI;
import carskit.alg.cars.adaptation.dependent.dev.CSLIM_C;
import carskit.alg.cars.adaptation.dependent.dev.CSLIM_CI;
import carskit.alg.cars.adaptation.dependent.dev.CSLIM_CU;
import carskit.alg.cars.adaptation.dependent.dev.CSLIM_CUCI;
import carskit.alg.cars.adaptation.dependent.dev.GCSLIM_CC;
import carskit.alg.cars.adaptation.dependent.sim.CAMF_ICS;
import carskit.alg.cars.adaptation.dependent.sim.CAMF_LCS;
import carskit.alg.cars.adaptation.dependent.sim.CAMF_MCS;
import carskit.alg.cars.adaptation.dependent.sim.CSLIM_ICS;
import carskit.alg.cars.adaptation.dependent.sim.CSLIM_LCS;
import carskit.alg.cars.adaptation.dependent.sim.CSLIM_MCS;
import carskit.alg.cars.adaptation.dependent.sim.GCSLIM_ICS;
import carskit.alg.cars.adaptation.dependent.sim.GCSLIM_LCS;
import carskit.alg.cars.adaptation.dependent.sim.GCSLIM_MCS;
import carskit.alg.cars.adaptation.independent.CPTF;
import carskit.alg.cars.transformation.hybridfiltering.DCR;
import carskit.alg.cars.transformation.hybridfiltering.DCW;
import carskit.alg.cars.transformation.prefiltering.ExactFiltering;
import carskit.alg.cars.transformation.prefiltering.SPF;
import carskit.alg.cars.transformation.prefiltering.splitting.ItemSplitting;
import carskit.alg.cars.transformation.prefiltering.splitting.UISplitting;
import carskit.alg.cars.transformation.prefiltering.splitting.UserSplitting;
import carskit.data.processor.DataSplitter;
import carskit.data.structure.SparseMatrix;
import carskit.generic.Recommender;
import carskit.gui.CARS;
import happy.coding.io.FileIO;
import happy.coding.io.LineConfiger;
import happy.coding.io.Logs;
import happy.coding.system.Dates;
import librec.data.MatrixEntry;

public class CARSRecommendation extends CARS {
	private Recommender algo;
	
	
	public static void main(String[] args) {
		try {
			new CARSRecommendation().execute(args);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
    public void execute(String[] args) throws Exception {
//    	Logs.debug("args: {}", args.length);
//    	for (int i = 0; i < args.length; i++) {
//    		Logs.debug("args[{}]: {}", i, args[i]);
//    	}
//    	
    	// get config
    	cmdLine(args);
        String user_id = "", config = "";
        int u = 0;

    	config = configFiles.get(0);

        // reset general settings
        preset(config);

        algo = getRecommender();
        try {
            algo.loadModel();
            Logs.debug("rateData is null: {}", algo.rateDao == null);
		} catch (Exception e) {
			Logs.error("Load model in Recommendation error: {}", e.toString());
//				e.printStackTrace();
		}

        // get user_id
        if (args != null && args.length > 3) {
        	LineConfiger paramOptions = new LineConfiger(args);
        	user_id = paramOptions.getString("-u");
    	} else if (cf.contains("input.user")) {
        	user_id = cf.getString("input.user");
        } else {
        	user_id = "-1";
        }

        // get list items
        String item_path = WorkingPath + String.format("user%s_travel.txt", user_id);
        Set<String> items;
        Logs.debug("item path: {}", item_path);
        items = getItems(item_path);
        
        // get list contexts
        String context_path = WorkingPath + String.format("user%s_context.txt", user_id);
        Logs.debug("context path: {}", context_path);
        Set<String> contexts = getContexts(context_path);        

        // convert user_id to index
        try {
	        if (user_id.compareTo("-1") != 0) {
	        	Logs.info("Recommender for user : {}", user_id);
	        	u = algo.rateDao.getUserId(user_id);
	        } else {
	        	Logs.info("Get all user");
	        }
        } catch (Exception e) {
        	Logs.warn("Don't have user for user_id: {}", user_id);
        	user_id = "-1";
        	Logs.info("Get all user");
		}
       
//        for (Entry<String, Integer> context:algo.rateDao.getContextConditionIds().entrySet()) {
//        	Logs.debug("key: {}", context.getKey());
//        }
//    
//        Logs.info("numContexts: {}", algo.rateDao.numContexts());
//        for (int c = 0; c < algo.rateDao.numContexts(); c++) {
//        	Logs.debug("{} {} {}", c, algo.rateDao.getContextId(c), algo.rateDao.getContextSituationFromInnerId(c));
//        	Logs.debug("context: {}", algo.rateDao.getContextId(algo.rateDao.getContextId(c)));
//        	Logs.debug("predict: {}", algo.getPredict(u, 0, c));
//        }
            
//        algo.getSize();
        
        // get recommends
        Map<String, Double> recommends = new HashMap<String, Double>();
        Logs.info("items size: {}", items.size());
        Logs.info("contexts size: {}", contexts.size());
        int j, c;
        Double predict;
        for (String item:items) {
        	Double max_pre = 0.0;
    		j = algo.rateDao.getItemId(item);
        	for (String ctx:contexts) {
        		c = algo.rateDao.getContextId(ctx);
        		if (user_id.compareTo("-1") == 0) {
        			predict = 0.0;
        			for (Integer u2:algo.rateDao.getUserListIds()) {
        				predict += algo.getPredict(u2, j, c);
        			}
        			Logs.info("average all user with item {} in context {} : {}", item, ctx, predict / algo.rateDao.numUsers());
    				max_pre = Double.max(max_pre, predict / algo.rateDao.numUsers());
        		} else {
        			predict = algo.getPredict(u, j, c);
        			Logs.info("user {} with item {} in context {} : {}", user_id, item, ctx, predict);
            		max_pre = Double.max(max_pre, predict);
        		}
        	}
        	recommends.put(item, max_pre);
        }
        String path = WorkingPath + String.format("user%s_suggestion.txt", user_id);
        writeRecommends(recommends, path);
    }
    
    private Set<String> getItems(String item_path) {
    	Set<String> items = new HashSet<String>();
    	try {
    		BufferedReader reader = new BufferedReader(new FileReader(item_path));
    		String line = reader.readLine();
    		while (line != null) {
        		items.add(line);
        		line = reader.readLine();
    		}
    		reader.close();
		} catch (Exception e) {
			Logs.warn("Can't get items: {}", e.toString());
			Logs.info("Get all items");
			return algo.rateDao.getItemList();
		}
    	return items;
    }

    private Set<String> getContexts(String context_path) {
    	Set<String> contexts = new HashSet<String>();
    	try {
    		BufferedReader reader = new BufferedReader(new FileReader(context_path));
    		String line = reader.readLine();
    		while (line != null) {
    			contexts.add(line);
        		line = reader.readLine();
    		}
    		reader.close();
		} catch (Exception e) {
			Logs.warn("Can't get contexts: {}", e.toString());
			Logs.info("Get all contexts");
			return algo.rateDao.getContextList();
		}
    	return contexts;
    }
    
    private void writeRecommends(Map<String, Double> recommends, String path) {
    	try {
    		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
    		for (Entry<String, Double> item:recommends.entrySet()) {
    			writer.write(String.format("%s,%s\n", item.getKey(), item.getValue().toString()));
    		}
    		writer.close();
    	} catch (Exception e) {
			// TODO: handle exception
		}
    }

    public Recommender getRecommender() throws Exception {

        algorithm = cf.getString("recommender");
        LineConfiger algOptions = new LineConfiger(algorithm);
        String option = algOptions.getMainParam().toLowerCase();
        if (!algorithmChoosen.isEmpty())
            option = algorithmChoosen.toLowerCase();

        switch (option) {

            case "globalavg":
                return new GlobalAverage();
            case "useravg":
                return new UserAverage();
            case "itemavg":
                return new ItemAverage();
            case "contextavg":
                return new ContextAverage();
            case "useritemavg":
                return new UserItemAverage();
            case "usercontextavg":
                return new UserContextAverage();
            case "itemcontextavg":
                return new ItemContextAverage();
            case "itemknn":
                return new ItemKNN();
            case "userknn":
                return new UserKNN();
            case "slopeone":
                return new SlopeOne();
            case "biasedmf":
                return new BiasedMF();
            case "pmf":
                return new PMF();
            case "bpmf":
                return new BPMF();
            case "nmf":
                return new NMF();
            case "svd++":
                return new SVDPlusPlus();
            case "slim":
                return new SLIM();
            case "bpr":
                return new BPR();
            case "lrmf":
                return new LRMF();
            case "rankals":
                return new RankALS();
            case "ranksgd":
                return new RankSGD();
            case "usersplitting":
            {
                String recsys_traditional=algOptions.getString("-traditional").trim().toLowerCase();
//                int minListLenU=algOptions.getInt("-minlenu", 2);
//                UserSplitting usp=new UserSplitting(rateDao.numUsers(),rateDao.getConditionContextsList(), rateDao.getURatedList());
//                Table<Integer, Integer, Integer> userIdMapper=usp.split(trainMatrix, minListLenU);
                Logs.info("User Splitting is done... Algorithm '"+recsys_traditional+"' will be applied to the transformed data set.");
                Recommender recsys=null;
                switch(recsys_traditional)
                {
                    ///////// baseline algorithms //////////////////////////////////////////////////////////
                    case "globalavg":
                        recsys=new GlobalAverage();break;
                    case "useravg":
                        recsys=new UserAverage();break;
                    case "itemavg":
                        recsys=new ItemAverage();break;
                    case "contextavg":
                        recsys=new ContextAverage();break;
                    case "useritemavg":
                        recsys=new UserItemAverage();break;
                    case "usercontextavg":
                        recsys=new UserContextAverage();break;
                    case "itemcontextavg":
                        recsys=new ItemContextAverage();break;
                    case "itemknn":
                        recsys=new ItemKNN();break;
                    case "userknn":
                        recsys=new UserKNN();break;
                    case "slopeone":
                        recsys=new SlopeOne();break;
                    case "biasedmf":
                        recsys=new BiasedMF();break;
                    case "pmf":
                        recsys=new PMF();break;
                    case "bpmf":
                        recsys=new BPMF(); break;
                    case "nmf":
                        recsys=new NMF(); break;
                    case "svd++":
                        recsys=new SVDPlusPlus(); break;
                    case "slim":
                        recsys=new SLIM();break;
                    case "bpr":
                        recsys=new BPR();break;
                    case "lrmf":
                        recsys=new LRMF();break;
                    case "rankals":
                        recsys=new RankALS();break;
                    case "ranksgd":
                        recsys=new RankSGD();break;
                    default:
                        recsys=null;
                }
                if(recsys==null)
                    throw new Exception("No recommender is specified!");
                else
                {
//                    recsys.setIdMappers(userIdMapper,null);
                    return recsys;
                }
            }

            ///////// Context-aware Splitting algorithms //////////////////////////////////////////////////////////
            case "itemsplitting":
            {
                String recsys_traditional=algOptions.getString("-traditional").trim().toLowerCase();
//                int minListLenI=algOptions.getInt("-minleni", 2);
//                ItemSplitting isp=new ItemSplitting(rateDao.numItems(),rateDao.getConditionContextsList(), rateDao.getIRatedList());
//                Table<Integer, Integer, Integer> itemIdMapper=isp.split(trainMatrix, minListLenI);
                Logs.info("Item Splitting is done... Algorithm '"+recsys_traditional+"' will be applied to the transformed data set.");
                Recommender recsys=null;
                switch(recsys_traditional)
                {

                    case "globalavg":
                        recsys=new GlobalAverage();break;
                    case "useravg":
                        recsys=new UserAverage();break;
                    case "itemavg":
                        recsys=new ItemAverage();break;
                    case "contextavg":
                        recsys=new ContextAverage();break;
                    case "useritemavg":
                        recsys=new UserItemAverage();break;
                    case "usercontextavg":
                        recsys=new UserContextAverage();break;
                    case "itemcontextavg":
                        recsys=new ItemContextAverage();break;
                    case "itemknn":
                        recsys=new ItemKNN();break;
                    case "userknn":
                        recsys=new UserKNN();break;
                    case "slopeone":
                        recsys=new SlopeOne();break;
                    case "biasedmf":
                        recsys=new BiasedMF();break;
                    case "pmf":
                        recsys=new PMF();break;
                    case "bpmf":
                        recsys=new BPMF(); break;
                    case "nmf":
                        recsys=new NMF(); break;
                    case "svd++":
                        recsys=new SVDPlusPlus(); break;
                    case "slim":
                        recsys=new SLIM();break;
                    case "bpr":
                        recsys=new BPR();break;
                    case "lrmf":
                        recsys=new LRMF();break;
                    case "rankals":
                        recsys=new RankALS();break;
                    case "ranksgd":
                        recsys=new RankSGD();break;
                    default:
                        recsys=null;
                }
                if(recsys==null)
                    throw new Exception("No recommender is specified!");
                else
                {
//                    recsys.setIdMappers(null, itemIdMapper);
                    return recsys;
                }
            }
            case "uisplitting":
            {
                String recsys_traditional=algOptions.getString("-traditional").trim().toLowerCase();
//                int minListLenU=algOptions.getInt("-minlenu", 2);
//                int minListLenI=algOptions.getInt("-minleni", 2);
//                UISplitting sp=new UISplitting(rateDao.numUsers(), rateDao.numItems(), rateDao.getConditionContextsList(), rateDao.getURatedList(), rateDao.getIRatedList());
//                Table<Integer, Integer, Integer> itemIdMapper=sp.splitItem(trainMatrix, minListLenI);
//                Table<Integer, Integer, Integer> userIdMapper=sp.splitUser(trainMatrix, minListLenU);
                Logs.info("UI Splitting is done... Algorithm '"+recsys_traditional+"' will be applied to the transformed data set.");
                Recommender recsys=null;
                switch(recsys_traditional)
                {

                    case "globalavg":
                        recsys=new GlobalAverage();break;
                    case "useravg":
                        recsys=new UserAverage();break;
                    case "itemavg":
                        recsys=new ItemAverage();break;
                    case "contextavg":
                        recsys=new ContextAverage();break;
                    case "useritemavg":
                        recsys=new UserItemAverage();break;
                    case "usercontextavg":
                        recsys=new UserContextAverage();break;
                    case "itemcontextavg":
                        recsys=new ItemContextAverage();break;
                    case "itemknn":
                        recsys=new ItemKNN();break;
                    case "userknn":
                        recsys=new UserKNN();break;
                    case "slopeone":
                        recsys=new SlopeOne();break;
                    case "biasedmf":
                        recsys=new BiasedMF();break;
                    case "pmf":
                        recsys=new PMF();break;
                    case "bpmf":
                        recsys=new BPMF(); break;
                    case "nmf":
                        recsys=new NMF(); break;
                    case "svd++":
                        recsys=new SVDPlusPlus(); break;
                    case "slim":
                        recsys=new SLIM();break;
                    case "bpr":
                        recsys=new BPR();break;
                    case "lrmf":
                        recsys=new LRMF();break;
                    case "rankals":
                        recsys=new RankALS();break;
                    case "ranksgd":
                        recsys=new RankSGD();break;
                    default:
                        recsys=null;
                }
                if(recsys==null)
                    throw new Exception("No recommender is specified!");
                else
                {
//                    recsys.setIdMappers(userIdMapper, itemIdMapper);
                    return recsys;
                }
            }

            case "exactfiltering":
            {
                return new ExactFiltering();
            }

            case "dcr":
            {
                return new DCR();
            }

            case "dcw":
            {
                return new DCW();
            }

            case "spf":
            {
                return new SPF();
            }

            ///////// Context-aware recommender: Tensor Factorization //////////////////////////////////////////////////////////
            case "cptf":
            {
                rateDao.LoadAsTensor();
                return new CPTF();
            }

            ///////// Context-aware recommender: CAMF //////////////////////////////////////////////////////////
            case "camf_c":
                return new CAMF_C();
            case "camf_ci":
                return new CAMF_CI();
            case "camf_cu":
                return new CAMF_CU();
            case "camf_cuci":
                return new CAMF_CUCI();
            case "camf_ics":
                return new CAMF_ICS();
            case "camf_lcs":
                return new CAMF_LCS();
            case "camf_mcs":
                return new CAMF_MCS();


            ///////// Context-aware recommender: CSLIM //////////////////////////////////////////////////////////
            case "cslim_c":
                return new CSLIM_C();
            case "cslim_cu":
                return new CSLIM_CU();
            case "cslim_ci":
                return new CSLIM_CI();
            case "cslim_cuci":
                return new CSLIM_CUCI();
            case "gcslim_cc":
                return new GCSLIM_CC();
            case "cslim_ics":
                return new CSLIM_ICS();
            case "cslim_lcs":
                return new CSLIM_LCS();
            case "cslim_mcs":
                return new CSLIM_MCS();
            case "gcslim_ics":
                return new GCSLIM_ICS();
            case "gcslim_lcs":
                return new GCSLIM_LCS();
            case "gcslim_mcs":
                return new GCSLIM_MCS();


            ////////////// Other context-aware recommendation algorithms /////////////////////////////////////////
            case "fm":
                return new FM();


            default:
                throw new Exception("No recommender is specified!");
        }
    }
}
