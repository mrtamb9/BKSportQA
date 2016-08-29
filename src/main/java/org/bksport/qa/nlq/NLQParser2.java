package org.bksport.qa.nlq;

import info.aduna.webapp.navigation.Group;

import java.io.PrintStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bksport.qa.Distance;
import org.bksport.qa.connector.KIMConnector;
import org.bksport.qa.mvc.ApplicationFacade;
import org.bksport.qa.util.CommandUtil;
import org.bksport.qa.util.DateUtil;
import org.bksport.qa.util.FileUtil;
import org.bksport.qa.util.NSUtil;
import org.bksport.sparql.AskQuery;
import org.bksport.sparql.Constraint;
import org.bksport.sparql.GroupGraphPattern;
import org.bksport.sparql.IRI;
import org.bksport.sparql.NewGroupGraphPattern;
import org.bksport.sparql.OrderCondiction;
import org.bksport.sparql.Query;
import org.bksport.sparql.RDFLiteral;
import org.bksport.sparql.SelectQuery;
import org.bksport.sparql.Triple;
import org.bksport.sparql.Var;
import org.bksport.sparql.expression.EqualExpression;
import org.bksport.sparql.function.StrFunction;

import com.hp.hpl.jena.rdf.arp.states.LookingForRDF;
import com.ontotext.kim.client.corpora.KIMAnnotation;
import com.ontotext.kim.client.corpora.KIMAnnotationSet;

import edu.stanford.nlp.io.StringOutputStream;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * 
 * Parser for natural language question to SPARQL query
 * 
 * @author congnh
 * 
 */
public class NLQParser2 {

	private static LexicalizedParser lp = LexicalizedParser
			.loadModel(FileUtil.getAbsolutePath("model", "standford-parser",
					"englishPCFG.ser.gz"));
	static KIMConnector connector;
	static {
		connector = new KIMConnector("localhost", 1099);
		connector.connect();
	}

	/**
	 * Parse a natural language query and return SPARQL query
	 * 
	 * @param nlQuestion
	 * @return SPARQL query if successful, null otherwise
	 * @throws UnsupportedQuestionException
	 */
	public static Query parse2(String nlQuestion) {
		// truncate command questions
		nlQuestion = CommandUtil.truncate(nlQuestion);
		nlQuestion = CommandUtil.preProcessTimeLable(nlQuestion);

		if (nlQuestion.toLowerCase().startsWith("result of the match")
				|| nlQuestion.toLowerCase().startsWith("result of match")) {
			nlQuestion = "What is " + nlQuestion.replace("Result", "result"); // de
																				// Result
																				// viet
																				// hoa
																				// se
																				// nhan
																				// sai
		} else if (nlQuestion.startsWith("Do ")) {
			nlQuestion = nlQuestion.replace("Do ", "do ");
		} else if (nlQuestion.startsWith("Does ")) {
			nlQuestion = nlQuestion.replace("Does ", "does ");
		}
		// PrintStream ps = System.out;
		// StringOutputStream sos = new StringOutputStream();
		// System.setOut(new PrintStream(sos));
		// Logger logger = Logger.getLogger(NLQParser.class);

		if (nlQuestion.contains("-")) {
			nlQuestion = nlQuestion.replaceAll("-", "and");
			System.out.println(nlQuestion);
		}
		if (nlQuestion.contains("vs")) {
			nlQuestion = nlQuestion.replaceAll("vs", "and");
		}
		if (nlQuestion.contains("New")) {
			nlQuestion = nlQuestion.replace("New", "new");
		}

		System.out.println(nlQuestion);

		TokenizerFactory<CoreLabel> tokenizerFactory;
		List<CoreLabel> rawWords2;
		Tree parse;
		List<Tree> leafList;
		Tree clauseTree;
		Tree whPhraseTree;
		Tree sqClauseTree;
		String clauseTag;
		TreebankLanguagePack tlp;
		GrammaticalStructureFactory gsf;
		GrammaticalStructure gs;
		List<TypedDependency> typedDepList;

		// parse query using Standford's Parser
		tokenizerFactory = PTBTokenizer
				.factory(new CoreLabelTokenFactory(), "");
		rawWords2 = tokenizerFactory.getTokenizer(new StringReader(nlQuestion))
				.tokenize();
		parse = lp.apply(rawWords2);
		leafList = parse.getLeaves(); // chuan bi bieu dien parse duoi dang cay
		clauseTree = parse.children()[0];
		whPhraseTree = null;
		sqClauseTree = null;
		clauseTag = clauseTree.label().value();
		tlp = new PennTreebankLanguagePack();
		gsf = tlp.grammaticalStructureFactory();
		gs = gsf.newGrammaticalStructure(parse);
		typedDepList = gs.typedDependenciesCCprocessed();

		// Tien xu ly cau hoi rut gon (Root = NP): news about Lionel Messi,
		// coach of Chelsea...
		if (clauseTag.toString().equals("NP")) {
			int i = 0;
			for (; i < typedDepList.size(); i++) {
				if (typedDepList.get(i).reln().getShortName().equals("prep")) {
					if (typedDepList.get(i).reln().getSpecific() != null) {
						break;
					}
				}
			}

			// Tao mot danh sach cac cum danh tu, noi cac danh tu rieng thanh
			// mot (vi du: Lionel-NNP Messi-NNP)
			ArrayList<String> listNNP = new ArrayList<String>();
			for (int j = 0; j < leafList.size(); j++) {
				// named entity (NNP, NNPS)
				Tree leaf = leafList.get(j);

				String tagName = leaf.ancestor(1, parse).value();

				if (tagName.startsWith("NNP")) // danh tu rieng
				{
					// merge
					String value = leaf.value();

					// Xu ly cum danh tu rieng: e.g. Lionel Messi
					while (j + 1 < leafList.size()
							&& (leafList.get(j + 1).ancestor(1, parse).value()
									.startsWith("NNP") || leafList.get(j + 1)
									.ancestor(1, parse).value().equals("CD"))) {
						value += " " + leafList.get(j + 1).value();
						j++;
					}
					listNNP.add(value);
				}
			}

			String preposition = typedDepList.get(i).reln().getSpecific()
					.toString();
			String subject = typedDepList.get(i).gov().value().toString();
			String object = typedDepList.get(i).dep().value().toString();

			// Noi cac danh tu rieng nhu Lionel-NNP Messi-NNP

			for (int j = 0; j < listNNP.size(); j++) {
				if (listNNP.get(j).contains(subject)) {
					subject = listNNP.get(j);
				}
				if (listNNP.get(j).contains(object)) {
					object = listNNP.get(j);
				}
			}

			// tim tu bo nghia cho subject (vi du: newest news, latest news...)
			// Truong hop "good news"... la truong hop khac, xu ly theo kieu
			// khac (chua)
			for (int j = 0; j < leafList.size(); j++) {
				// named entity (NNP, NNPS)
				Tree leaf = leafList.get(j);

				String tagName = leaf.ancestor(1, parse).value();

				if (tagName.startsWith("JJ")) // tinh tu
				{
					// merge
					String value = leaf.value();

					if (nlQuestion.contains(value + " " + subject)) {
						subject = value + " " + subject;
						;
						int k = j;
						// Xu ly cum danh tu rieng: e.g. Lionel Messi
						while (k - 1 > 0
								&& (leafList.get(k - 1).ancestor(1, parse)
										.value().startsWith("JJ"))) {
							subject = leafList.get(k - 1).value() + " "
									+ subject;
							k--;
						}
					} else if (nlQuestion.contains(value + " " + object)) {
						object = value + " " + object;
						;
						int k = j;
						// Xu ly cum danh tu rieng: e.g. Lionel Messi
						while (k - 1 > 0
								&& (leafList.get(k - 1).ancestor(1, parse)
										.value().startsWith("JJ"))) {
							object = leafList.get(k - 1).value() + " "
									+ subject;
							k--;
						}
					}
				}
			}

			nlQuestion = "Which " + subject + " is " + preposition + " "
					+ object + "?";

			// parse query using Standford's Parser the second time
			tokenizerFactory = PTBTokenizer.factory(
					new CoreLabelTokenFactory(), "");
			rawWords2 = tokenizerFactory.getTokenizer(
					new StringReader(nlQuestion)).tokenize();
			parse = lp.apply(rawWords2);
			leafList = parse.getLeaves(); // chuan bi bieu dien parse duoi dang
											// cay
			clauseTree = parse.children()[0];
			whPhraseTree = null;
			sqClauseTree = null;
			clauseTag = clauseTree.label().value();
			tlp = new PennTreebankLanguagePack();
			gsf = tlp.grammaticalStructureFactory();
			gs = gsf.newGrammaticalStructure(parse);
			typedDepList = gs.typedDependenciesCCprocessed();
		}

		System.out.println(nlQuestion);
		TreePrint tp = new TreePrint("penn, typedDependenciesCollapsed");
		tp.printTree(parse); // bieu dien parse duoi dang cay

		// recognize instance: nhan dang thuc the bang KIM
		List<ResourceEntry> reList = new ArrayList<ResourceEntry>();
		List<KIMAnnotation> bksportAnnotation = new ArrayList<KIMAnnotation>();
		KIMAnnotationSet kimAnnotationSet = connector.annotate(nlQuestion);
		Iterator<?> iterator = kimAnnotationSet.iterator();
		while (iterator.hasNext()) {
			KIMAnnotation annotation = (KIMAnnotation) iterator.next();
			if (annotation.getFeatures().containsKey("inst")) {
				if (annotation.getFeatures().get("inst").toString()
						.startsWith(NSUtil.bksport)) {
					bksportAnnotation.add(annotation);
					ResourceEntry re = new ResourceEntry();
					re.setResource(new IRI(annotation.getFeatures().get("inst")
							.toString()));
					re.setType(new IRI(annotation.getFeatures().get("class")
							.toString()));
					reList.add(re);
				}
			}
		}

		// phan tren la nhan dang thuc the co ten xuat hien trong nlQuestion
		// OUTPUT: List<ResourceEntry> reList: danh sach cac thuc the co ten
		// xuat hien trong nlQ
		System.out.println("TTT bksportAnnotation: " + bksportAnnotation);
		System.out.println("TTT instance: " + reList);

		Query query = null;
		GroupGraphPattern where = null;
		List<LexicalEntry> lexList = null;
		List<Integer> realPossitionList = null;
		List<DepEntry> depList = null;

		// recognize resource
		lexList = new ArrayList<LexicalEntry>();
		realPossitionList = new ArrayList<Integer>();
		Morphology mor = new Morphology();
		for (int i = 0; i < leafList.size(); i++) {
			LexicalEntry lexEntry = null;
			// named entity (NNP, NNPS)
			Tree leaf = leafList.get(i);

			String tagName = leaf.ancestor(1, parse).value();
			if (tagName.startsWith("NNP")) // danh tu rieng
			{
				// merge
				lexEntry = new LexicalEntry();
				lexEntry.setPosition(lexList.size()); // vi tri lexical trong
														// nlQ
				lexEntry.setTagName(tagName);
				lexList.add(lexEntry);
				realPossitionList.add(lexEntry.getPosition());
				String value = leaf.value();

				// Xu ly cum danh tu rieng: e.g. Lionel Messi
				while (i + 1 < leafList.size()
						&& (leafList.get(i + 1).ancestor(1, parse).value()
								.startsWith("NNP") || leafList.get(i + 1)
								.ancestor(1, parse).value().equals("CD"))) {
					value += " " + leafList.get(i + 1).value();
					i++;
					realPossitionList.add(lexEntry.getPosition());
				}
				lexEntry.setValue(value);

				ResourceEntry slotEntry = new ResourceEntry(new Var(), null);
				lexEntry.addSlotEntry(slotEntry);

				// TODO recognize instance?
				// done, but in pattern generator, need to move here

				for (int ni = 0; ni < bksportAnnotation.size(); ni++) {
					KIMAnnotation annotation = bksportAnnotation.get(ni);
					if (annotation.getFeatures().containsKey("originalName")) {
						if (value.contains(annotation.getFeatures()
								.get("originalName").toString())) {
							slotEntry.setResource(new IRI(annotation
									.getFeatures().get("inst").toString()));
							slotEntry.setType(new IRI(annotation.getFeatures()
									.get("class").toString()));
						}
					}
				}

				boolean found = false;
				for (int ni = 0; ni < reList.size(); ni++) {
					// Câu dưới --> String.contain(Resource) --> không được
					// Except: who is http://bk.sport.owl#lionel-mess?
					if (value.contains(reList.get(ni).getResource()
							.getResource())) {
						slotEntry.setResource(reList.get(ni).getResource());
						slotEntry.setType(reList.get(ni).getType());
						found = true;
					}
				}
				if (!found) {
					// System.out.println("TTT I think that it is always !found. Except: who is http://bk.sport.owl#lionel-messi?");
					try {
						// slotEntry.setResource(new IRI(distance.find(value)));
						slotEntry.setType(IRI.OWL_CLASS_IRI);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				// TTT khong phai danh tu rieng
				// Noun (NN, NNS) or Verb (VB, VBD, VBG, VBN, VBP, VBZ), etc.
				lexEntry = new LexicalEntry();
				lexEntry.setValue(leaf.value());
				lexEntry.setTagName(tagName);
				lexEntry.setPosition(lexList.size());
				lexList.add(lexEntry);
				realPossitionList.add(lexEntry.getPosition());
				HashSet<ResourceEntry> generalLexSet = ResourceRecognizer
						.getGeneralResourceEntrySet(mor.stem(lexEntry
								.getValue()));
				if (generalLexSet != null && !generalLexSet.isEmpty()) {
					for (ResourceEntry res : generalLexSet) {
						ResourceEntry slotEntry = new ResourceEntry(null,
								res.getType());
						if (res.getType().equals(IRI.OWL_CLASS_IRI)) {
							slotEntry.setResource(new Var());
							slotEntry.setType(res.getResource());
						} else if (res.getType().equals(
								IRI.OWL_OBJECTPROPERTY_IRI)) {
							slotEntry.setResource(res.getResource());
						} else if (res.getType().equals(
								IRI.OWL_DATATYPEPROPERTY_IRI)) {
							slotEntry.setResource(res.getResource());
						} else if (lexEntry.getValue().toLowerCase()
								.equals("today")) {
							slotEntry.setResource(DateUtil.getDateToday());
						} else if (lexEntry.getValue().toLowerCase()
								.equals("yesterday")) {
							slotEntry.setResource(DateUtil.getDateYesterday());
						} else {
							slotEntry.setResource(res.getResource());
							slotEntry.setResource(res.getType());
						}
						lexEntry.addSlotEntry(slotEntry);
					}
					// general term
				} else {
					// domain term
					HashSet<ResourceEntry> domainLexSet = ResourceRecognizer
							.getDomainResourceEntrySet(mor.stem(lexEntry
									.getValue()));

					if (domainLexSet != null && !domainLexSet.isEmpty()) {
						for (ResourceEntry res : domainLexSet) {
							ResourceEntry slotEntry = new ResourceEntry(null,
									res.getType());
							if (res.getType().equals(IRI.OWL_CLASS_IRI)) {
								slotEntry.setResource(new Var());
								slotEntry.setType(res.getResource());
							} else if (res.getType().equals(
									IRI.OWL_OBJECTPROPERTY_IRI)) {
								slotEntry.setResource(res.getResource());
							} else if (res.getType().equals(
									IRI.OWL_DATATYPEPROPERTY_IRI)) {
								slotEntry.setResource(res.getResource());
							} else {
								slotEntry.setResource(res.getResource());
								slotEntry.setResource(res.getType());
							}
							lexEntry.addSlotEntry(slotEntry); // có thể có nhiều
																// khả năng xảy
																// ra cho một
																// token, lấy
																// SlotEntry(0)
						}
					} else {
						lexEntry.addSlotEntry(new ResourceEntry(new Var(), null));
						if (lexEntry.getTagName().equals("."))
							lexList.remove(lexEntry);
					}
				}
			}
		}

		// TTT show the lexList
		for (int i = 0; i < lexList.size(); i++) {
			LexicalEntry lexEntry = lexList.get(i);
			System.out.println("lexEntry(" + i + "): "
					+ lexEntry.getSlotEntry(0).getType());
		}

		// ==========================================================================
		// TODO: Code Tâm
		// newQuery
		// ==========================================================================
		GroupGraphPattern newWhereOrClause = new GroupGraphPattern("graph");
		Var graphVar = new Var("graph");
		Query newQuery = null;
		List<DepEntry> depList2 = new ArrayList<DepEntry>();

		if (clauseTag.equals("S")) {
			newQuery = new SelectQuery();
			((SelectQuery)newQuery).addVar(graphVar);
		} else if (clauseTag.equals("FRAG")) {
			newQuery = new SelectQuery();
			((SelectQuery)newQuery).addVar(graphVar);
		} else if (clauseTag.equals("SBAR")) {
			newQuery = new SelectQuery();
			((SelectQuery)newQuery).addVar(graphVar);
		} else if (clauseTag.equals("SBARQ")) {
			newQuery = new SelectQuery();
			((SelectQuery)newQuery).addVar(graphVar);
			whPhraseTree = clauseTree.children()[0];
			sqClauseTree = clauseTree.children()[1];
		} else if (clauseTag.equals("SINV")) {
			// newQuery = null
		} else if (clauseTag.equals("SQ")) {
			newQuery = new AskQuery();
			sqClauseTree = clauseTree;
		} else {
			// newQuery = null
		}

		// xay dung depList2 && andList
		ArrayList<LexicalEntry> andList = new ArrayList<LexicalEntry>();
		System.out.println("TTT typedDepList: " + typedDepList);
		
		for (int i = 0; i < typedDepList.size(); i++) {
			TypedDependency typedDep = typedDepList.get(i);
			TreeGraphNode dep = typedDep.dep();
			TreeGraphNode gov = typedDep.gov();

			if (DepEntry.isSupportType(typedDep.reln().getShortName())) {
				DepEntry depEntry = new DepEntry();
				depEntry.setType(typedDep.reln().getShortName());
				if (typedDep.reln().getSpecific() != null && !typedDep.reln().getSpecific().equals("null")) 
				{
					depEntry.setSpecific(typedDep.reln().getSpecific());
				}
				
				depEntry.setDep(lexList.get(realPossitionList.get(dep.index() - 1))); 
				depEntry.setGov(lexList.get(realPossitionList.get(gov.index() - 1)));

				depList2.add(depEntry); // TTT xay dung depList2

				if (depEntry.getType().equals("conj")) 
				{
					if (!andList.contains(depEntry.getDep())) 
					{
						andList.add(depEntry.getDep());
					}
					if (!andList.contains(depEntry.getGov())) 
					{
						andList.add(depEntry.getGov());
					}
				}
			}
		}

		// print andList
		if (andList.size() > 0) {
			System.out.println("Exist and-list: ");
			for (LexicalEntry lexEntry : andList) {
				System.out.print(lexEntry.getValue() + "\t");
			}
			System.out.println();
		}

		if (newQuery != null) {
			System.out.println("newQuery != null");
			newQuery.addPrefix("bksport", new IRI(NSUtil.bksport));
			newQuery.addPrefix("owl", IRI.OWL_IRI);
			newQuery.addPrefix("rdf", IRI.RDF_IRI);
			newQuery.addPrefix("rdfs", IRI.RDFS_IRI);
			newQuery.addPrefix("time", IRI.TIME_IRI);
			newQuery.setWhere(newWhereOrClause);

			// TTT
			int nsubjIndex = -1;
			int nsubjpassIndex = -1;
			int typedOtherIndex = -1;

			ArrayList<DepEntry> sp = new ArrayList<DepEntry>(); // subject-predicate
			ArrayList<DepEntry> po = new ArrayList<DepEntry>(); // predicate-object

			// Noi nsubject (S V) & dobject (V O) sinh --> pattern ?s ?v ?o

			System.out.println("TTT depList2: " + depList2);

			for (int i = 0; i < depList2.size(); i++) {
				DepEntry depEntryBefore = depList2.get(i);
				if (depEntryBefore.getType().equals("nsubj")) {
					nsubjIndex++;
				} else if (depEntryBefore.getType().equals("nsubjpass")) {
					nsubjpassIndex++;
				}

				for (int j = i + 1; j < depList2.size(); j++) // Duyet qua tung cap Dependence (A, B)
				{
					DepEntry depEntryAfter = depList2.get(j);

					// Doan nay de noi (A, B) && (B, C) ---> (A, B, C)
					if (depEntryBefore.getDep() == depEntryAfter.getDep()) {
						// not yet
					}
					if (depEntryBefore.getDep() == depEntryAfter.getGov()) {
						// not yet
					}

					if (depEntryBefore.getGov() == depEntryAfter.getDep()) {
						// not yet
					}

					if (depEntryBefore.getGov() == depEntryAfter.getGov()) {
						if (depEntryBefore.getType().equals("nsubj")) {
							if (depEntryAfter.getType().equals("dobj") || depEntryAfter.getType().equals("prep") || depEntryAfter.getType().equals("advmod") || depEntryAfter.getType().equals("attr") || depEntryAfter.getType().equals("dep")) {
								// xu ly rieng cho prep: prep_in khong tinh'
								if (!depEntryAfter.getType().equals("prep")) {
									typedOtherIndex++;
									sp.add(depEntryBefore);
									po.add(depEntryAfter);
								}
								// 14-06-2015 (nguyen nhan: Where is FIFA located in?)
								// chi xu ly nhung dang prep_abc(), prep() khong xu ly.
								else if (depEntryAfter.getSpecific() != null) 
								{
									if (!depEntryAfter.getSpecific().equals("in")) 
									{
										typedOtherIndex++;
										sp.add(depEntryBefore);
										po.add(depEntryAfter);
									}
								}
							}
						}

						if (depEntryAfter.getType().equals("nsubj")) 
						{
							if (depEntryBefore.getType().equals("dobj") || depEntryBefore.getType().equals("prep") || depEntryBefore.getType().equals("advmod")
									|| depEntryBefore.getType().equals("attr") || depEntryBefore.getType().equals("dep")) 
							{
								// xu ly rieng cho prep: prep_in khong tinh'
								if (!depEntryBefore.getType().equals("prep")) 
								{
									typedOtherIndex++;
									sp.add(depEntryAfter);
									po.add(depEntryBefore);
								} 
								else if (depEntryBefore.getSpecific() != null) 
								{
									if (!depEntryBefore.getSpecific().equals("in")) 
									{
										typedOtherIndex++;
										sp.add(depEntryAfter);
										po.add(depEntryBefore);
									}
								}
							}
						}

						if (depEntryBefore.getType().equals("nsubjpass")) 
						{
							if (depEntryAfter.getType().equals("agent")
									|| depEntryAfter.getType().equals("attr")
									|| depEntryAfter.getType().equals("dep")
									|| depEntryAfter.getType().equals("prep")
									|| depEntryAfter.getType().equals("advmod")) 
							{
								if (!depEntryAfter.getType().equals("prep") && !depEntryAfter.getType().equals("advmod")) 
								{
									typedOtherIndex++;
									sp.add(depEntryAfter);
									po.add(depEntryBefore);
								} 
								else if (depEntryAfter.getType().equals("advmod")) 
								{
									typedOtherIndex++;
									sp.add(depEntryBefore);
									po.add(depEntryAfter);
								}								
								else if (depEntryAfter.getSpecific() != null) 
								{ 
									// xu ly rieng cho prep: prep_in khong tinh'
									if (!depEntryAfter.getSpecific().equals("in")) 
									{
										typedOtherIndex++;
										sp.add(depEntryBefore);
										po.add(depEntryAfter);
									}
								}
							}
						}

						if (depEntryAfter.getType().equals("nsubjpass")) 
						{
							if (depEntryBefore.getType().equals("agent")
									|| depEntryBefore.getType().equals("attr")
									|| depEntryBefore.getType().equals("dep")
									|| depEntryBefore.getType().equals("prep")
									|| depEntryBefore.getType().equals("advmod")) 
							{
								if (!depEntryBefore.getType().equals("prep")
										&& !depEntryBefore.getType().equals("advmod")) 
								{
									typedOtherIndex++;
									sp.add(depEntryBefore);
									po.add(depEntryAfter);
								} else if (depEntryBefore.getType().equals("advmod")) 
								{
									typedOtherIndex++;
									sp.add(depEntryAfter);
									po.add(depEntryBefore);
								}
								// xu ly rieng cho prep: prep_in khong tinh'
								else if (depEntryBefore.getSpecific() != null) 
								{
									if (!depEntryBefore.getSpecific().equals("in")) 
									{
										typedOtherIndex++;
										sp.add(depEntryAfter);
										po.add(depEntryBefore);
									}
								}
							}
						}
						if (depEntryBefore.getType().equals("attr")) 
						{
							// xay dung cho attr - prep_about()
							if (depEntryAfter.getType().equals("prep") && depEntryAfter.getSpecific() != null) 
							{
								sp.add(depEntryBefore);
								po.add(depEntryAfter);
							}
						}
						/*
						 * Service for "Which news is about Chelsea?"
						 * det(news-2, Which-1) attr(is-3, news-2) root(ROOT-0,
						 * is-3) prep_about(is-3, Chelsea-5)
						 */
						
						if (depEntryAfter.getType().equals("attr")) 
						{
							// xay dung cho attr - prep_about()
							if (depEntryBefore.getType().equals("prep") && depEntryBefore.getSpecific() != null) 
							{
								sp.add(depEntryAfter);
								po.add(depEntryBefore);
							}
						}
					}
				}
			}

			System.out.println("TTT sp: " + sp);
			System.out.println(("TTT po: " + po));

			Set<DepEntry> labeledOrSet = new HashSet<DepEntry>();

			boolean doOr1 = false;
			boolean doOr2 = false;
			boolean doOr3 = false;
			boolean doOr4 = false;
			String DESCRIPTION_RESULT = "description_result";
			// String ENTITY_RESULT = "entity_result";
			String PERSON_ORGANIZATION_RESULT = "person_organization_result";
			String PERSON = "person";
			String OBJECT = "object";
			String LOCATION_RESULT = "location_result";
			String NUMBER_RESULT = "number_result";
			String expectedResult = null;

			// Khai bao mot conjList de tranh truong hop trung lap between:
			// What happened between Barca and Chelsea?
			/*
			 * GRAPH ?g { <http://bk.sport.owl#Barcelona-FC> ?x19
			 * <http://bk.sport.owl#chelsea-fc>. ?x19 <rdfs:subPropertyOf>
			 * <http://bk.sport.owl#happen>. <http://bk.sport.owl#Barcelona-FC>
			 * ?x20 <http://bk.sport.owl#chelsea-fc>. ?x20 <rdfs:subPropertyOf>
			 * <http://bk.sport.owl#happen>. }
			 */
			List<DepEntry> conjList = new ArrayList<DepEntry>();
			for (int i = 0; i < sp.size(); i++) 
			{
				DepEntry depEntryBefore = sp.get(i);
				DepEntry depEntryAfter = po.get(i);
				if (depEntryBefore.getDep().getTagName().startsWith("NNP") && !(doOr1)) 
				{
					if (depEntryBefore.getDep().getSlotEntry(0).getResource().getClass() == Var.class && !labeledOrSet.contains(depEntryBefore))
					{
						System.out.println(true + "1");
						labeledOrSet.add(depEntryBefore);
						Var label = new Var();
						newWhereOrClause.addTriple(new Triple(
								depEntryBefore.getDep().getSlotEntry(0).getResource(),
								new IRI("rdfs", "label"), 
								label));
						
						newWhereOrClause.addFilter(new Constraint(new EqualExpression(new StrFunction(label),new RDFLiteral(depEntryBefore.getDep().getValue()))));
					}
				} else if (depEntryBefore.getDep().getTagName().startsWith("NN") && !(doOr2)) 
				{
					if (depEntryBefore.getDep().getSlotEntry(0).getType() != null) 
					{
						System.out.println(true + "2");
						newWhereOrClause.addTriple(new Triple(
								depEntryBefore.getDep().getSlotEntry(0).getResource(), 
								new IRI("rdf", "type"), 
								depEntryBefore.getDep().getSlotEntry(0).getType()));
					}
				}

				if (depEntryAfter.getDep().getTagName().startsWith("NNP") && !(doOr3)) 
				{
					if (depEntryAfter.getDep().getSlotEntry(0).getResource().getClass() == Var.class && !labeledOrSet.contains(depEntryAfter)) 
					{
						System.out.println(true + "3");
						labeledOrSet.add(depEntryAfter);
						Var label = new Var();
						newWhereOrClause.addTriple(new Triple(
								depEntryAfter.getDep().getSlotEntry(0).getResource(), 
								new IRI("rdfs", "label"), 
								label));
						
						newWhereOrClause.addFilter(new Constraint(new EqualExpression(new StrFunction(label), new RDFLiteral(depEntryAfter.getDep().getValue()))));
					}

				} else if (depEntryAfter.getDep().getTagName().startsWith("NN") && !(doOr4)) 
				{
					if (depEntryAfter.getDep().getSlotEntry(0).getType() != null) 
					{
						System.out.println(true + "4");
						newWhereOrClause.addTriple(new Triple(
								depEntryAfter.getDep().getSlotEntry(0).getResource(), 
								new IRI("rdf", "type"), 
								depEntryAfter.getDep().getSlotEntry(0).getType()));
					}
				}

				// Xu ly voi Prep_...
				// tranh bi them quan he voi prep_in
				if (depEntryAfter.getType().equals("prep")) // doan code phia duoi rat thon
				{
					if (depEntryAfter.getSpecific() == null) 
					{
						if(depEntryBefore.getGov().getSlotEntry(0).getResource().toString()
								.compareToIgnoreCase("<http://bk.sport.owl#playFor>")==0)
						{
							newWhereOrClause.addTriple(new Triple(
									depEntryBefore.getDep().getSlotEntry(0).getResource(), 
									depEntryBefore.getGov().getSlotEntry(0).getResource(), 
									depEntryAfter.getDep().getSlotEntry(0).getResource()));
						} else {
							newWhereOrClause.addTriplePattern(new Triple(
									depEntryBefore.getDep().getSlotEntry(0).getResource(), 
									depEntryBefore.getGov().getSlotEntry(0).getResource(), 
									depEntryAfter.getDep().getSlotEntry(0).getResource()));
						}
						
					} else if (!depEntryAfter.getSpecific().equals("in")) // tranh bi them quan he khi co prep_in
					{
						if (depEntryAfter.getSpecific().equals("to")) 
						{
							// xu ly "happen"
							if (!depEntryBefore.getGov().getSlotEntry(0).getResource().getResource().equals("http://bk.sport.owl#happen")) {
								newWhereOrClause.addTriplePattern(new Triple(depEntryBefore.getDep().getSlotEntry(0).getResource(), depEntryBefore.getGov().getSlotEntry(0).getResource(), depEntryAfter.getDep().getSlotEntry(0).getResource()));
							} 
							else 
							{
								Var v = new Var();
								// ((SelectQuery) newQuery).addVar(v); // Muon them vao nhung neu them se khong dung thu tu <subject> <predicate>

								newWhereOrClause.addTriplePattern(new Triple(depEntryBefore.getDep().getSlotEntry(0).getResource(), v, depEntryAfter.getDep().getSlotEntry(0).getResource()));

								newWhereOrClause.addTriple(new Triple(v, new IRI("rdfs:subPropertyOf"), new IRI("http://bk.sport.owl#happen")));
							}
						} 
						else if (depEntryAfter.getSpecific().equals("between")) // not "to"
						{
							for (int j = 0; j < typedDepList.size(); j++) 
							{
								TypedDependency typedDep = typedDepList.get(j);
								TreeGraphNode dep = typedDep.dep();
								TreeGraphNode gov = typedDep.gov();
								// conj - link between two (content) words
								// connected by a conjunction, e.g. 'Bill is big
								// and honest' - conj(big,honest)
								if (typedDep.reln().getShortName().equals("conj") && typedDep.reln().getSpecific().equals("and")) {
									DepEntry depEntry = new DepEntry();
									depEntry.setType(typedDep.reln().getShortName());
									if (typedDep.reln().getSpecific() != null && !typedDep.reln().getSpecific().equals("null")) 
									{
										depEntry.setSpecific(typedDep.reln().getSpecific());
									}
									
									depEntry.setDep(lexList.get(realPossitionList.get(dep.index() - 1)));
									depEntry.setGov(lexList.get(realPossitionList.get(gov.index() - 1)));

									// kiem tra xem depEntry co ton tai trong
									// conjList chua?
									int check = 0;
									for (int k = 0; k < conjList.size(); k++) 
									{
										if (depEntry.getDep().getSlotEntry(0).getResource().getResource().equals(conjList.get(k).getDep().getSlotEntry(0).getResource().getResource())
												&& depEntry.getGov().getSlotEntry(0).getResource().getResource().equals(conjList.get(k).getGov().getSlotEntry(0).getResource().getResource())) 
										{
											check = 1;
											break;
										}
									}

									if (check == 0) // Chua ton tai depEntry trong conjList
									{
										conjList.add(depEntry);
										if (!depEntryBefore.getGov().getSlotEntry(0).getResource().getResource().equals("http://bk.sport.owl#happen")) 
										{
											newQuery.getWhere().addTriplePattern(new Triple(depEntry.getGov().getSlotEntry(0).getResource(), depEntryBefore.getGov().getSlotEntry(0).getResource(),depEntry.getDep().getSlotEntry(0).getResource()));
										} else {
											newQuery.getWhere().addTriplePattern(new Triple(
																	depEntry.getGov().getSlotEntry(0).getResource(),
																	lexList.get(0).getSlotEntry(0).getResource(),
																	depEntry.getDep().getSlotEntry(0).getResource()));

											newQuery.getWhere().addTriple(
															new Triple(lexList.get(0).getSlotEntry(0).getResource(),
																	new IRI("rdfs:subPropertyOf"),
																	new IRI("http://bk.sport.owl#happen")));
										}
									}
								}
							}
						} else if (depEntryAfter.getSpecific().equals("about")) 
						{
							// xu ly: which news is about something..?
							if (depEntryBefore.getGov().getValue().equals("is")
									|| depEntryBefore.getGov().getValue().equals("was")
									|| depEntryBefore.getGov().getValue().equals("were")
									|| depEntryBefore.getGov().getValue().equals("'s")
									|| depEntryBefore.getGov().getValue().equals("are")
									|| depEntryBefore.getGov().getValue().equals("'re")) 
							{
								newWhereOrClause.addTriple(new Triple(
										depEntryBefore.getDep().getSlotEntry(0).getResource(), 
										new IRI("http://bk.sport.owl#about"),
										depEntryAfter.getDep().getSlotEntry(0).getResource()));
								
//								newWhereOrClause.addTriple(new Triple(
//										new Var("graph"), 
//										new IRI("http://bk.sport.owl#about"),
//										depEntryAfter.getDep().getSlotEntry(0).getResource()));

								((SelectQuery)newQuery).removeVar(graphVar);
								
//								newWhereOrClause.addFilterString("SAMETERM(?graph, " + depEntryBefore.getDep().getSlotEntry(0).getResource() + ")");
							}
						} else 
						{
							if(depEntryBefore.getGov().getSlotEntry(0).getResource().toString()
									.compareToIgnoreCase("<http://bk.sport.owl#playFor>")==0)
							{
								newWhereOrClause.addTriple(new Triple(
										depEntryBefore.getDep().getSlotEntry(0).getResource(), 
										depEntryBefore.getGov().getSlotEntry(0).getResource(), 
										depEntryAfter.getDep().getSlotEntry(0).getResource()));
							} else {
								newWhereOrClause.addTriplePattern(new Triple(
										depEntryBefore.getDep().getSlotEntry(0).getResource(), 
										depEntryBefore.getGov().getSlotEntry(0).getResource(), 
										depEntryAfter.getDep().getSlotEntry(0).getResource()));
							}
						}
					}
				} else if (depEntryBefore.getGov().getValue().equals("is")
						|| depEntryBefore.getGov().getValue().equals("was")
						|| depEntryBefore.getGov().getValue().equals("were")
						|| depEntryBefore.getGov().getValue().equals("'s")
						|| depEntryBefore.getGov().getValue().equals("are")
						|| depEntryBefore.getGov().getValue().equals("'re")) 
				{
					newWhereOrClause.addTriple(new Triple(depEntryBefore.getDep().getSlotEntry(0).getResource(), 
							new IRI(newQuery, "bksport", "hasAbstract"), 
							depEntryAfter.getDep().getSlotEntry(0).getResource()));
					
					expectedResult = null; // khong them abstract nua.
				} else {
					// === Truong hop cuoi cung ===
					if(depEntryBefore.getGov().getSlotEntry(0).getResource().toString()
							.compareToIgnoreCase("<http://bk.sport.owl#contain>")==0)
					{
						newWhereOrClause.addTriple(new Triple(
								depEntryBefore.getDep().getSlotEntry(0).getResource(), 
								depEntryBefore.getGov().getSlotEntry(0).getResource(), 
								depEntryAfter.getDep().getSlotEntry(0).getResource()));
						
//						newWhereOrClause.addTriple(new Triple(
//								new Var("graph"), 
//								new IRI("http://bk.sport.owl#contain"),
//								depEntryAfter.getDep().getSlotEntry(0).getResource()));
						
						((SelectQuery)newQuery).removeVar(graphVar);
						
//						newWhereOrClause.addFilterString("SAMETERM(?graph, " + depEntryBefore.getDep().getSlotEntry(0).getResource() + ")");
					
					} else if(depEntryBefore.getGov().getSlotEntry(0).getResource().toString()
							.compareToIgnoreCase("<http://bk.sport.owl#playFor>")==0)
					{
						newWhereOrClause.addTriple(new Triple(
								depEntryBefore.getDep().getSlotEntry(0).getResource(), 
								depEntryBefore.getGov().getSlotEntry(0).getResource(), 
								depEntryAfter.getDep().getSlotEntry(0).getResource()));
					} else {
						newWhereOrClause.addTriplePattern(new Triple(
								depEntryBefore.getDep().getSlotEntry(0).getResource(), 
								depEntryBefore.getGov().getSlotEntry(0).getResource(), 
								depEntryAfter.getDep().getSlotEntry(0).getResource()));
					}

					// xu ly rieng truong hop say/talk, mention about.
					if (depEntryBefore.getGov().getValue().equals("say") 
							|| depEntryBefore.getGov().getValue().equals("talk")
							|| depEntryBefore.getGov().getValue().equals("mention")) 
					{
						// xem xet su ton tai cua prep_about()
						int j = 0;
						DepEntry prepDepEntry = new DepEntry();
						for (j = 0; j < depList2.size(); j++) 
						{
							if (depList2.get(j).getType().equals("prep") && depList2.get(j).getSpecific() != null) 
							{
								if (depList2.get(j).getSpecific().equals("about")) 
								{
									prepDepEntry = depList2.get(j);
									break; // nhay ra khoi vong lap for
								}
							}
						}

						if (j < depList2.size()) // --> ton tai prep_about()
						{
							if (prepDepEntry.getDep().getNumOfSlotEntry() > 0
									&& prepDepEntry.getDep().getSlotEntry(0).getResource().getResource() != null) 
							{
								newWhereOrClause.addTriple(new Triple(
										depEntryAfter.getDep().getSlotEntry(0).getResource(),
										IRI.RDF_TYPE_IRI, 
										new IRI("rdfs", "Statement")));

								GroupGraphPattern x1 = new GroupGraphPattern("graph");
								x1.addTriple(new Triple(depEntryAfter.getDep().getSlotEntry(0).getResource(),
										new IRI("rdf", "subject"), 
										prepDepEntry.getDep().getSlotEntry(0).getResource()));

								GroupGraphPattern x2 = new GroupGraphPattern("graph");
								x2.addTriple(new Triple(depEntryAfter.getDep().getSlotEntry(0).getResource(),
										new IRI("rdf", "object"), 
										prepDepEntry.getDep().getSlotEntry(0).getResource()));

								newWhereOrClause.addUnion(x1);
								newWhereOrClause.addUnion(x2);
							} else {
								// todo1
								Var var = new Var(prepDepEntry.getDep().getSlotEntry(0).getResource().toString());
								newWhereOrClause.addFilter(new Constraint(
												new EqualExpression(
												new StrFunction(var),
												new RDFLiteral(prepDepEntry.getDep().getValue()))));
							}
						}
					}
				}
			}

			// Hau xu ly,
			// Cau co dang: who scored yesterday?, Did Messi score yesterday?,
			// Did Messi score in the match? (cau nay van dang bi loi)
			// Was Barca defeated.
			System.out.println("nsubj: " + nsubjIndex + " - pass: "
					+ nsubjpassIndex + " - other: " + typedOtherIndex);
			if (nsubjIndex >= 0 && typedOtherIndex < 0) {
				for (int i = 0; i < depList2.size(); i++) {
					DepEntry temp = depList2.get(i);
					if (temp.getType().equals("nsubj")) {
						Var object = new Var();
						newWhereOrClause
								.addTriplePattern(new Triple(temp.getDep()
										.getSlotEntry(0).getResource(),
										temp.getGov().getSlotEntry(0)
												.getResource(), object));
					}
				}
			} else if (nsubjpassIndex >= 0 && typedOtherIndex < 0) {
				for (int i = 0; i < depList2.size(); i++) {
					DepEntry temp = depList2.get(i);
					if (temp.getType().equals("nsubjpass")) {
						Var subject = new Var();
						newWhereOrClause.addTriplePattern(new Triple(subject,
								temp.getGov().getSlotEntry(0).getResource(),
								temp.getDep().getSlotEntry(0).getResource()));
					}
				}
			}

			// xu ly voi prep_about, prep_in
			// nam ngoai newQuery == null
			for (int i = 0; i < typedDepList.size(); i++) 
			{
				TypedDependency typedDep = typedDepList.get(i);
				TreeGraphNode dep = typedDep.dep();
				TreeGraphNode gov = typedDep.gov();
				if (DepEntry.isSupportType(typedDep.reln().getShortName())) 
				{
					DepEntry depEntry = new DepEntry();
					depEntry.setType(typedDep.reln().getShortName());
					if (typedDep.reln().getSpecific() != null
							&& !typedDep.reln().getSpecific().equals("null")) 
					{
						depEntry.setSpecific(typedDep.reln().getSpecific());
					}
					
					depEntry.setDep(lexList.get(realPossitionList.get(dep.index() - 1)));
					depEntry.setGov(lexList.get(realPossitionList.get(gov.index() - 1)));

					if (depEntry.getType().equals("prep")) 
					{
						if (depEntry.getSpecific() != null) 
						{
							if (depEntry.getSpecific().equals("in")) 
							{
								if (depEntry.getDep().getTagName().startsWith("CD")) // dd/MM/yyyy
								{
									CommandUtil cmu = new CommandUtil();
									if (cmu.checkDateFormat(depEntry.getDep().getValue().toString()) > 0) // check date format
									{
										String dateBegin = new String("\""
												+ cmu.getYYYY() + "-"
												+ cmu.getMMBegin() + "-"
												+ cmu.getDDBegin()
												+ "\"^^<xsd:dateTime>");

										String dateEnd = new String("\""
												+ cmu.getYYYY() + "-"
												+ cmu.getMMEnd() + "-"
												+ cmu.getDDEnd()
												+ "\"^^<xsd:dateTime>");

										Var time = new Var("t");
										Var instantDate = new Var("instantDate");

										newWhereOrClause
												.addTriple(new Triple(
														graphVar,
														new IRI(
																"http://bk.sport.owl#hasTime"),
														time));

										newWhereOrClause.addTriple(new Triple(
												time, new IRI(newQuery, "rdf",
														"type"), new IRI(
														newQuery, "time",
														"Instant")));

										newWhereOrClause.addTriple(new Triple(
												time, new IRI(newQuery, "time",
														"inXSDDateTime"),
												instantDate));

										String filter = instantDate.toString()
												+ " >= " + dateBegin + " && "
												+ instantDate.toString()
												+ " <= " + dateEnd;

										newWhereOrClause.addFilterDate(filter);
									}
								}
							}
						}
					}
				}
			}

			if ((clauseTag.equals("S") || clauseTag.equals("SBAR")
					|| clauseTag.equals("SBARQ") || clauseTag.equals("SQ"))
					&& newQuery != null) {
				if (whPhraseTree != null) {
					List<Tree> sb = whPhraseTree.getLeaves();
					for (int i = 0; i < sb.size(); i++) {
						String value = sb.get(i).value().toLowerCase();
						if (value.startsWith("wh")) {
							if (value.equals("where")) {
								expectedResult = LOCATION_RESULT;
							} else if (value.equals("when")) {
								expectedResult = NUMBER_RESULT;
							} else if (value.equals("whom")) {
								expectedResult = PERSON_ORGANIZATION_RESULT;
							} else if (value.equals("who")) {
								// TTT
								// System.err.println("Line 1332: who");
								expectedResult = PERSON;
							} else if (value.equals("which")) {
								// System.err.println("Line 1335: which");
								expectedResult = OBJECT;
							} else if (value.equals("what")) {

							}
						}
					}
				}

				// TODO
				System.out.println("LexList: " + lexList);
				// recognize returned object in SELECT query
				if (whPhraseTree != null || nlQuestion.toLowerCase().contains("whom")) 
				{
					for (int i = 0; i < lexList.size(); i++) 
					{
						if (lexList.get(i).getTagName().startsWith("W")) 
						{
							if (lexList.get(i).getTagName().equals("WDT")) 
							{
								// which
								if (nlQuestion.toLowerCase().contains("latest news")) 
								{
									System.err.println("latest news");
									i = i + 1; // i = 0:which; 1:latest; 2:news;
								}
								
								if(lexList.get(i + 1).getSlotEntry(0).getType()!=null)
								{
									newWhereOrClause.addTriple(
											new Triple(lexList.get(i + 1).getSlotEntry(0).getResource(),
											new IRI("rdf", "type"), 
											lexList.get(i + 1).getSlotEntry(0).getType()));
								}

								((SelectQuery) newQuery).addVar((Var) lexList.get(i + 1).getSlotEntry(0).getResource());

								if (nlQuestion.toLowerCase().contains(" news ")) 
								{
									if (nlQuestion.toLowerCase().contains("latest news")) 
									{
										System.out.println("TTT: latest news");
										Var t = new Var("t");
										for (int j = 0; j < depList2.size(); j++) 
										{
											DepEntry dep = depList2.get(j);
											System.out.println(dep.getType());
											if (dep.getType().equals("dep")) 
											{
												newWhereOrClause.addTriple(new Triple(
																dep.getDep().getSlotEntry(0).getResource(),
																new IRI("http://bk.sport.owl#isPublishedOn"),
																t));
												
												OrderCondiction ord = new OrderCondiction(t);
												ord.setDesc(true);
												((SelectQuery) newQuery).getSolution().addCondiction(ord);
											}
										}
									}

									if (lexList.get(i + 1).getSlotEntry(0).getType() != null) 
									{
										newWhereOrClause.addTriple(new Triple(lexList.get(i + 1).getSlotEntry(0).getResource(),
														new IRI("rdf", "type"), 
														lexList.get(i + 1).getSlotEntry(0).getType()));
									}
								}
							} else if (lexList.get(i).getTagName().equals("WP")) {// what, who, whom

								((SelectQuery) newQuery).addVar((Var) lexList.get(i).getSlotEntry(0).getResource());

								if (expectedResult == DESCRIPTION_RESULT) {
									Var abstr = new Var();

									((SelectQuery) newQuery).addVar(abstr);

									newWhereOrClause.addTriple(new Triple(
											(Var) lexList.get(i).getSlotEntry(0).getResource(), 
											new IRI(newQuery, "bksport", "hasAbstract"), 
											abstr));
								} else if(expectedResult == PERSON)
								{
									/*newWhereOrClause.addTriple(
											new Triple(lexList.get(i).getSlotEntry(0).getResource(),
											new IRI("rdf", "type"), 
											"<http://bk.sport.owl#Person>"
											));*/
								}
							} else if (lexList.get(i).getTagName().equals("WP$")) 
							{
								// whose
							} else if (lexList.get(i).getTagName().equals("WRB")) 
							{
								// where,
								// when, how
								if (lexList.get(i).getValue().equals("where")) 
								{
									Var label = new Var();
									if (lexList.get(i).getNumOfSlotEntry() > 0) // TTT
																				// getNumOfSlotEntry
																				// ???
									{
										newWhereOrClause.addTriple(new Triple(
												(Var) lexList.get(i).getSlotEntry(0).getResource(),
												new IRI(newQuery, "rdfs", "label"), 
												label));

										newWhereOrClause.addTriple(new Triple(
														(Var) lexList.get(i).getSlotEntry(0).getResource(),
														new IRI(newQuery, "rdf", "type"),
														new IRI(newQuery,"bksport", "Location")));
									}

									((SelectQuery) newQuery).addVar(label);

								} else if (lexList.get(i).getValue().toLowerCase().equals("how")) 
								{
									if (lexList.get(i + 1).getValue().toLowerCase().equals("many")) 
									{
										((SelectQuery) newQuery).addVar(new Var(
														"COUNT(" + lexList.get(i + 2).getSlotEntry(0).getResource() + ")", ' '));
									}
								}
							}
						}
					}
				} // end if whPhraseTree != null
			}

			// FILTER
			if (lookingForDepEntry(depList2, "num") < depList2.size()) {
				int indexNum = lookingForDepEntry(depList2, "num");
				String quantValue = depList2.get(indexNum).getDep().getValue()
						.toString();

				if (lexList.get(0).getValue().toString()
						.compareToIgnoreCase("who") == 0) {
					newWhereOrClause.addgroupcountList("GROUP BY "
							+ lexList.get(0).getSlotEntry(0).getResource());
				}
				if (lexList.get(0).getValue().toString()
						.compareToIgnoreCase("which") == 0) {
					newWhereOrClause.addgroupcountList("GROUP BY "
							+ lexList.get(1).getSlotEntry(0).getResource());
				}

				if (lookingForDepEntry(depList2, "quantmod") < depList2.size()) {
					if (lookingForDepEntry(depList2, "mwe") < depList2.size()) {
						int indexMwe = lookingForDepEntry(depList2, "mwe");
						String category = depList2.get(indexMwe).getDep()
								.getValue().toString();
						// at most
						// at least
						if (category.equals("least")) {
							newWhereOrClause.addhavingcountList("COUNT("
									+ depList2.get(indexNum).getGov()
											.getSlotEntry(0).getResource()
									+ ")" + " >= " + quantValue);
						} else if (category.equals("more")) {
							newWhereOrClause.addhavingcountList("COUNT("
									+ depList2.get(indexNum).getGov()
											.getSlotEntry(0).getResource()
									+ ")" + " > " + quantValue);
						} else if (category.equals("less")) {
							newWhereOrClause.addhavingcountList("COUNT("
									+ depList2.get(indexNum).getGov()
											.getSlotEntry(0).getResource()
									+ ")" + " < " + quantValue);
						}
					}
				} else {
					newWhereOrClause.addhavingcountList("COUNT("
							+ depList2.get(indexNum).getGov().getSlotEntry(0)
									.getResource() + ")" + " = " + quantValue);
				}
			}

			// ORDER BY - THE MOST - THE LEAST
			if (lookingForDepEntry(depList2, "det") < depList2.size()
					&& lookingForDepEntry(depList2, "amod") < depList2.size()) {
				int indexDet = lookingForDepEntry(depList2, "det");
				int indexAmod = lookingForDepEntry(depList2, "amod");
				if (depList2
						.get(indexDet)
						.getGov()
						.getValue()
						.toString()
						.equals(depList2.get(indexAmod).getGov().getValue()
								.toString())
						&& depList2.get(indexDet).getDep().getValue()
								.toString().equals("the")) {
					// GROUP BY
					if (lexList.get(0).getValue().toString()
							.compareToIgnoreCase("who") == 0) {
						newWhereOrClause.addgroupcountList("GROUP BY "
								+ lexList.get(0).getSlotEntry(0).getResource());
					}
					if (lexList.get(0).getValue().toString()
							.compareToIgnoreCase("which") == 0) {
						newWhereOrClause.addgroupcountList("GROUP BY "
								+ lexList.get(1).getSlotEntry(0).getResource());
					}

					String category = depList2.get(indexAmod).getDep()
							.getValue().toString();
					if (category.equals("most")) {
						newWhereOrClause.addOrderByList("ORDER BY DESC(COUNT("
								+ depList2.get(indexAmod).getGov()
										.getSlotEntry(0).getResource()
										.toString() + ")) OFFSET 0 LIMIT 1");
					} else if (category.equals("least")) {
						newWhereOrClause.addOrderByList("ORDER BY ASC(COUNT("
								+ depList2.get(indexAmod).getGov()
										.getSlotEntry(0).getResource()
										.toString() + ")) OFFSET 0 LIMIT 1");
					}
				}
			}

			// TTT: chi danh cho What is result of match between Team and Team?
			if (nlQuestion.toLowerCase().contains("what")
					&& nlQuestion.toLowerCase().contains("result of")
					&& nlQuestion.toLowerCase().contains("match")
					&& nlQuestion.toLowerCase().contains("between")) {
				// Duyet bien result thay vi tao mot bien moi
				Var rs = null;
				for (int i = 0; i < lexList.size(); i++) {
					if (lexList.get(i).getValue().startsWith("result")) {
						rs = new Var(lexList.get(i).getSlotEntry(0)
								.getResource().getResource());
						break;
					}
				}

				if (rs == null) {
					rs = new Var();
				}

				Var mtch = new Var(); // Tao bien moi match
				((SelectQuery) newQuery).addVar(rs);
				newQuery.getWhere().addTriplePattern(
						new Triple(mtch, new IRI(
								"http://bk.sport.owl#hasResult"), rs));
				newQuery.getWhere().addTriple(
						new Triple(rs, IRI.RDF_TYPE_IRI, new IRI(
								"http://bk.sport.owl#MatchResult")));
				newQuery.getWhere().addTriple(
						new Triple(mtch, IRI.RDF_TYPE_IRI, new IRI(
								"http://bk.sport.owl#Match")));
				newQuery.getWhere().addTriplePattern(
						new Triple(mtch, new IRI(
								"http://bk.sport.owl#firstCompetitor"), andList
								.get(0).getSlotEntry(0).getResource()));
				newQuery.getWhere().addTriplePattern(
						new Triple(mtch, new IRI(
								"http://bk.sport.owl#secondCompetitor"),
								andList.get(1).getSlotEntry(0).getResource()));

				System.out.println("TTT return newQuery");
				// return newQuery; // not return query
			}
		}
		// ///// newQuery == null ///////
		else {
			System.out.println("newQuery == null");

			newQuery = new SelectQuery();
			newQuery.addPrefix("bksport", new IRI(NSUtil.bksport));
			newQuery.addPrefix("owl", IRI.OWL_IRI);
			newQuery.addPrefix("rdf", IRI.RDF_IRI);
			newQuery.addPrefix("rdfs", IRI.RDFS_IRI);
			newQuery.addPrefix("time", IRI.TIME_IRI);
			newQuery.setWhere(newWhereOrClause);

			// Them bien (news) vao select (voi cau: news about Messi? Thing about Messi?)
			for (int i = 0; i < lexList.size(); i++) 
			{
				if (lexList.get(i).getTagName().startsWith("NN")) // vi du: news[NN] about Messi?
				{
					// System.out.println("?x about... ?x = " +
					// lexList.get(i).getValue());
					((SelectQuery) newQuery).addVar((Var) lexList.get(i).getSlotEntry(0).getResource());

					// trường hợp có news thì có quan hệ bksport:hasURL
					if (nlQuestion.toLowerCase().contains("news")) 
					{
						// Var url = new Var();
						// ((SelectQuery) newQuery).addVar(url);
						//TTT:27-03-2016 newWhereOrClause.addTriple(new Triple(lexList.get(i).getSlotEntry(0).getResource(), new IRI(newQuery, "bksport", "hasURL"), url));

						// TODO2 Congnh
						if (nlQuestion.toLowerCase().contains("latest news")) {
							System.out.println("TTT: latest news");
							Var t = new Var("t");
							// System.out.println("TTTTTTTTTTTT depList2: " +
							// depList2.toString());
							for (int j = 0; j < depList2.size(); j++) 
							{
								DepEntry dep = depList2.get(j);
								System.out.println(dep.getType());
								if (dep.getType().equals("dep")) 
								{
									newWhereOrClause.addTriple(new Triple(dep.getDep().getSlotEntry(0).getResource(),
													new IRI("http://bk.sport.owl#isPublishedOn"), t));
									OrderCondiction ord = new OrderCondiction(t);
									ord.setDesc(true);
									((SelectQuery) newQuery).getSolution()
											.addCondiction(ord);
								}
							}
						}
					}

					if (lexList.get(i).getSlotEntry(0).getType() != null) 
					{
						newWhereOrClause.addTriple(new Triple(lexList.get(i)
								.getSlotEntry(0).getResource(), new IRI("rdfs",
								"type"), lexList.get(i).getSlotEntry(0)
								.getType()));
					}
					break;
				}
			}
		}

		System.out.println("-- newQuery --\n" + newQuery.toString());
		System.out.println();

		// ApplicationFacade.getInstance().sendNotification(ApplicationFacade.LOG_CMD,
		// sos.toString());
		// System.setOut(ps);

		return newQuery;
	}

	// tim kiem trong ListDepEntry xem co ton tai typedDepList type khong?
	private static int lookingForDepEntry(List<DepEntry> list, String type) {
		int i;
		for (i = 0; i < list.size(); i++) {
			if (list.get(i).getType().equals(type)) {
				break;
			}
		}

		return i;
	}

}
