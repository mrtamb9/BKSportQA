package org.bksport.qa.tmp;

import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bksport.qa.Distance;
import org.bksport.qa.connector.KIMConnector;
import org.bksport.qa.mvc.ApplicationFacade;
import org.bksport.qa.nlq.DepEntry;
import org.bksport.qa.nlq.LexicalEntry;
import org.bksport.qa.nlq.ResourceEntry;
import org.bksport.qa.nlq.ResourceRecognizer;
import org.bksport.qa.util.CommandUtil;
import org.bksport.qa.util.DateUtil;
import org.bksport.qa.util.FileUtil;
import org.bksport.qa.util.NSUtil;
import org.bksport.sparql.AskQuery;
import org.bksport.sparql.Constraint;
import org.bksport.sparql.GroupGraphPattern;
import org.bksport.sparql.IRI;
import org.bksport.sparql.Query;
import org.bksport.sparql.RDFLiteral;
import org.bksport.sparql.SelectQuery;
import org.bksport.sparql.Triple;
import org.bksport.sparql.Var;
import org.bksport.sparql.expression.EqualExpression;
import org.bksport.sparql.function.StrFunction;

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
public class NLQParser {

	private static LexicalizedParser lp = LexicalizedParser
			.loadModel(FileUtil.getAbsolutePath("model", "standford-parser",
					"englishPCFG.ser.gz"));
	static KIMConnector connector;
	// static Distance distance = new Distance();
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
	public static Query parse(String nlQuestion) {
		if (nlQuestion.toLowerCase().startsWith("result of the match")
				|| nlQuestion.toLowerCase().startsWith("result of match")) {
			nlQuestion = "What is " + nlQuestion;
		}
		PrintStream ps = System.out;
		StringOutputStream sos = new StringOutputStream();
		 System.setOut(new PrintStream(sos));
		// Logger logger = Logger.getLogger(NLQParser.class);
		System.out.println(nlQuestion);
		// truncate command questions
		nlQuestion = CommandUtil.truncate(nlQuestion);
		if (nlQuestion.contains("-"))
			nlQuestion.replaceAll("-", "and");
		if (nlQuestion.contains("vs"))
			nlQuestion.replaceAll("vs", "and");

		if (nlQuestion.contains("New")) {
			nlQuestion = nlQuestion.replace("New", "new");
		}
		List<ResourceEntry> reList = new ArrayList<ResourceEntry>();
		// recognize instance
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
		// parse query using Standford's Parser
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		List<CoreLabel> rawWords2 = tokenizerFactory.getTokenizer(
				new StringReader(nlQuestion)).tokenize();
		Tree parse = lp.apply(rawWords2);

		List<Tree> leafList = parse.getLeaves();
		Tree clauseTree = parse.children()[0];
		Tree whPhraseTree = null;
		Tree sqClauseTree = null;
		String clauseTag = clauseTree.label().value();

		Query query = null;
		GroupGraphPattern where = null;
		List<LexicalEntry> lexList = null;
		List<Integer> realPossitionList = null;
		List<DepEntry> depList = null;

		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> typedDepList = gs.typedDependenciesCCprocessed();

		TreePrint tp = new TreePrint("penn, typedDependenciesCollapsed");
		tp.printTree(parse);

		// recognize resource
		lexList = new ArrayList<LexicalEntry>();
		realPossitionList = new ArrayList<Integer>();
		Morphology mor = new Morphology();
		for (int i = 0; i < leafList.size(); i++) {
			LexicalEntry lexEntry = null;
			// named entity (NNP, NNPS)
			Tree leaf = leafList.get(i);
			String tagName = leaf.ancestor(1, parse).value();
			if (tagName.startsWith("NNP")) {
				// merge
				lexEntry = new LexicalEntry();
				lexEntry.setPosition(lexList.size());
				lexEntry.setTagName(tagName);
				lexList.add(lexEntry);
				realPossitionList.add(lexEntry.getPosition());
				String value = leaf.value();
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
					if (value.contains(reList.get(ni).getResource()
							.getResource())) {
						slotEntry.setResource(reList.get(ni).getResource());
						slotEntry.setType(reList.get(ni).getType());
						found = true;
					}
				}
				if (!found) {
					try {
						// slotEntry.setResource(new IRI(distance.find(value)));
						slotEntry.setType(IRI.OWL_CLASS_IRI);
						;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {// Noun (NN, NNS) or Verb (VB, VBD, VBG, VBN, VBP, VBZ),
					// etc.
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
							lexEntry.addSlotEntry(slotEntry);
						}
					} else {
						lexEntry.addSlotEntry(new ResourceEntry(new Var(), null));
						if (lexEntry.getTagName().equals("."))
							lexList.remove(lexEntry);
					}
				}
			}
		}

		for (int i = 0; i < lexList.size(); i++) {
			LexicalEntry lexEntry = lexList.get(i);
			System.out.println(lexEntry);
			// for(int j=0;j<lexEntry.getNumOfSlotEntry();j++){
			// System.out.println(lexEntry.getSlotEntry(j).getResourceEntry());
			// }
		}

		// recognize sentence type
		System.out.println("%%" + clauseTag);
		if (clauseTag.equals("S")) {
			query = new SelectQuery();
		} else if (clauseTag.equals("SBAR")) {
			query = new SelectQuery();
		} else if (clauseTag.equals("SBARQ")) {
			query = new SelectQuery();
			whPhraseTree = clauseTree.children()[0];
			sqClauseTree = clauseTree.children()[1];
		} else if (clauseTag.equals("SINV")) {
		} else if (clauseTag.equals("SQ")) {
			query = new AskQuery();
			sqClauseTree = clauseTree;
		} else {
			// do nothing
		}

		System.out.println("query=" + (query != null));

		if (query != null) {
			where = new GroupGraphPattern();
			query.addPrefix("bksport", new IRI(NSUtil.bksport));
			query.addPrefix("owl", IRI.OWL_IRI);
			query.addPrefix("rdf", IRI.RDF_IRI);
			query.addPrefix("rdfs", IRI.RDFS_IRI);
			query.setWhere(where);

			String DESCRIPTION_RESULT = "description_result";
			String ENTITY_RESULT = "entity_result";
			String PERSON_ORGANIZATION_RESULT = "person_organization_result";
			String LOCATION_RESULT = "location_result";
			String NUMBER_RESULT = "number_result";
			String expectedResult = null;
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

						} else if (value.equals("which")) {

						} else if (value.equals("what")) {

						}
					}
				}
			}

			int nsubjIndex = -1;
			int nsubjpassIndex = -1;
			int dobjIndex = -1;
			int pobjIndex = -1;
			int iobjIndex = -1;
			int attrIndex = -1;
			int agentIndex = -1;
			int prepIndex = -1;
			int advmodIndex = -1;
			DepEntry nsubjDepEntry = null;
			DepEntry nsubjpassDepEntry = null;
			DepEntry dobjDepEntry = null;
			DepEntry pobjDepEntry = null;
			DepEntry iobjDepEntry = null;
			DepEntry attrDepEntry = null;
			DepEntry agentDepEntry = null;
			DepEntry prepDepEntry = null;
			DepEntry advmodDepEntry = null;
			depList = new ArrayList<DepEntry>();
			for (int i = 0; i < typedDepList.size(); i++) {
				TypedDependency typedDep = typedDepList.get(i);
				TreeGraphNode dep = typedDep.dep();
				TreeGraphNode gov = typedDep.gov();
				if (DepEntry.isSupportType(typedDep.reln().getShortName())) {
					DepEntry depEntry = new DepEntry();
					depEntry.setType(typedDep.reln().getShortName());
					if (typedDep.reln().getSpecific() != null
							&& !typedDep.reln().getSpecific().equals("null")) {
						depEntry.setSpecific(typedDep.reln().getSpecific());
					}
					depEntry.setDep(lexList.get(realPossitionList.get(dep
							.index() - 1)));
					depEntry.setGov(lexList.get(realPossitionList.get(gov
							.index() - 1)));
					depList.add(depEntry);
					if (depEntry.getType().equals("nsubj")) {
						nsubjIndex = depList.size() - 1;
						nsubjDepEntry = depEntry;
					}
					if (depEntry.getType().equals("nsubjpass")) {
						nsubjpassIndex = depList.size() - 1;
						nsubjpassDepEntry = depEntry;
					}
					if (depEntry.getType().equals("dobj")) {
						dobjIndex = depList.size() - 1;
						dobjDepEntry = depEntry;
					}
					if (depEntry.getType().equals("pobj")) {
						pobjIndex = depList.size() - 1;
						pobjDepEntry = depEntry;
					}
					if (depEntry.getType().equals("iobj")) {
						iobjIndex = depList.size() - 1;
						iobjDepEntry = depEntry;
					}
					if (depEntry.getType().equals("attr")) {
						attrIndex = depList.size() - 1;
						attrDepEntry = depEntry;
					}
					if (depEntry.getType().equals("agent")) {
						agentIndex = depList.size() - 1;
						agentDepEntry = depEntry;
					}
					if (depEntry.getType().equals("prep")) {
						prepIndex = depList.size() - 1;
						prepDepEntry = depEntry;
					}
					if (depEntry.getType().equals("advmod")) {
						advmodIndex = depList.size() - 1;
						advmodDepEntry = depEntry;
					}
				}
			}

			if (nsubjIndex >= 0) {
				if (dobjIndex >= 0) {
					if (nsubjDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (nsubjDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						if (nsubjDepEntry.getDep().getSlotEntry(0).getType() != null)
							query.getWhere()
									.addTriplePattern(
											new Triple(nsubjDepEntry.getDep()
													.getSlotEntry(0)
													.getResource(), new IRI(
													"rdf", "type"),
													nsubjDepEntry.getDep()
															.getSlotEntry(0)
															.getType()));
					}
					if (dobjDepEntry.getDep().getTagName().startsWith("NNP")) {

						if (dobjDepEntry.getDep().getSlotEntry(0).getResource()
								.getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(dobjDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(dobjDepEntry
													.getDep().getValue()))));
						}
					} else if (dobjDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						if (dobjDepEntry.getDep().getSlotEntry(0).getType() != null)
							query.getWhere()
									.addTriplePattern(
											new Triple(dobjDepEntry.getDep()
													.getSlotEntry(0)
													.getResource(), new IRI(
													"rdf", "type"),
													dobjDepEntry.getDep()
															.getSlotEntry(0)
															.getType()));
					}
					query.getWhere().addTriplePattern(
							new Triple(nsubjDepEntry.getDep().getSlotEntry(0)
									.getResource(), nsubjDepEntry.getGov()
									.getSlotEntry(0).getResource(),
									dobjDepEntry.getDep().getSlotEntry(0)
											.getResource()));
				} else if (attrIndex >= 0) {
					if (attrDepEntry.getGov().getValue().equals("is")
							|| attrDepEntry.getGov().getValue().equals("was")
							|| attrDepEntry.getGov().getValue().equals("were")
							|| attrDepEntry.getGov().getValue().equals("'s")) {
						if (nsubjDepEntry.getDep().getTagName()
								.startsWith("NNP")) {
							if (nsubjDepEntry.getDep().getSlotEntry(0)
									.getResource().getClass() == Var.class) {
								Var label = new Var();
								query.getWhere()
										.addTriplePattern(
												new Triple(
														attrDepEntry
																.getDep()
																.getSlotEntry(0)
																.getResource(),
														new IRI("rdfs", "label"),
														label));
								query.getWhere()
										.addFilter(
												new Constraint(
														new EqualExpression(
																new StrFunction(
																		label),
																new RDFLiteral(
																		nsubjDepEntry
																				.getDep()
																				.getValue()))));
								expectedResult = DESCRIPTION_RESULT;
							}
							query.getWhere().addTriplePattern(
									new Triple(nsubjDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI(query, "bksport",
													"hasAbstract"),
											attrDepEntry.getDep()
													.getSlotEntry(0)
													.getResource()));
						} else if (nsubjDepEntry.getDep().getTagName()
								.startsWith("NN")) {
							// TODO: who is manager --> hasAbstract
							// What is manager --> hasAbstract
							// Who is manager of Barcenola --> find person
							// What is team of Lionel Messi --> find team
							if (prepIndex >= 0) {
								// TODO: null pointer problem when
								// "who is manager of MU"
								// without concept manager!!!
								query.getWhere().addTriplePattern(
										new Triple(attrDepEntry.getDep()
												.getSlotEntry(0).getResource(),
												new IRI(query, "rdf", "type"),
												nsubjDepEntry.getDep()
														.getSlotEntry(0)
														.getType()));
								// TODO: thi���u ph���n �����nh ngh��a v���
								// att...Dep
							} else {
								query.getWhere().addTriplePattern(
										new Triple(nsubjDepEntry.getDep()
												.getSlotEntry(0).getResource(),
												new IRI(query, "bksport",
														"hasAbstract"),
												attrDepEntry.getDep()
														.getSlotEntry(0)
														.getResource()));
							}
							System.out.println("*******"
									+ attrDepEntry.getGov().getValue());
							// query.getWhere().addTriplePattern(
							// new Triple(nsubjDepEntry.getDep().getSlotEntry(0)
							// .getResourceEntry().getResource(), new IRI(query,
							// "bksport", "hasAbstract"), attrDepEntry.getDep()
							// .getSlotEntry(0).getSlot()));
						}
						// TODO: maybe redundant
						// query.getWhere().addTriplePattern(
						// new
						// Triple(nsubjDepEntry.getDep().getSlotEntry(0).getSlot(),
						// nsubjDepEntry.getGov().getSlotEntry(0).getSlot(),
						// attrDepEntry.getDep().getSlotEntry(0).getSlot()));
					}
				} else if (prepIndex >= 0 && prepDepEntry.getSpecific() != null) {
					if (nsubjDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (nsubjDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						if (nsubjDepEntry.getDep().getSlotEntry(0).getType() != null)
							query.getWhere()
									.addTriplePattern(
											new Triple(nsubjDepEntry.getDep()
													.getSlotEntry(0)
													.getResource(), new IRI(
													"rdf", "type"),
													nsubjDepEntry.getDep()
															.getSlotEntry(0)
															.getType()));
					}
					if (prepDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (prepDepEntry.getDep().getSlotEntry(0).getResource()
								.getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(prepDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(prepDepEntry
													.getDep().getValue()))));
						}
					} else if (prepDepEntry.getDep().getTagName()
							.startsWith("NN")
							&& prepDepEntry.getDep().getSlotEntry(0).getType() != null
							&& prepDepEntry.getDep().getSlotEntry(0).getType()
									.equals(IRI.OWL_CLASS_IRI)) {
						query.getWhere().addTriplePattern(
								new Triple(prepDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"), prepDepEntry
												.getDep().getSlotEntry(0)
												.getType()));
					}
					if (prepDepEntry.getSpecific().equals("to")) {
						if (!nsubjDepEntry.getGov().getSlotEntry(0)
								.getResource().getResource()
								.equals("http://bk.sport.owl#happen")) {
							query.getWhere().addTriplePattern(
									new Triple(nsubjDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											nsubjDepEntry.getGov()
													.getSlotEntry(0)
													.getResource(),
											prepDepEntry.getDep()
													.getSlotEntry(0)
													.getResource()));
						} else {
							Var v = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjDepEntry.getDep()
											.getSlotEntry(0).getResource(), v,
											prepDepEntry.getDep()
													.getSlotEntry(0)
													.getResource()));
							query.getWhere()
									.addTriplePattern(
											new Triple(
													v,
													new IRI(
															"rdfs:subPropertyOf"),
													new IRI(
															"http://bk.sport.owl#happen")));
						}
					} else if (prepDepEntry.getSpecific().equals("between")) {
						for (int i = 0; i < typedDepList.size(); i++) {
							TypedDependency typedDep = typedDepList.get(i);
							TreeGraphNode dep = typedDep.dep();
							TreeGraphNode gov = typedDep.gov();
							if (typedDep.reln().getShortName().equals("conj")
									&& typedDep.reln().getSpecific()
											.equals("and")) {
								DepEntry depEntry = new DepEntry();
								depEntry.setType(typedDep.reln().getShortName());
								if (typedDep.reln().getSpecific() != null
										&& !typedDep.reln().getSpecific()
												.equals("null")) {
									depEntry.setSpecific(typedDep.reln()
											.getSpecific());
								}
								depEntry.setDep(lexList.get(realPossitionList
										.get(dep.index() - 1)));
								depEntry.setGov(lexList.get(realPossitionList
										.get(gov.index() - 1)));
								depList.add(depEntry);
								query.getWhere().addTriplePattern(
										new Triple(depEntry.getGov()
												.getSlotEntry(0).getResource(),
												nsubjDepEntry.getDep()
														.getSlotEntry(0)
														.getResource(),
												prepDepEntry.getDep()
														.getSlotEntry(0)
														.getResource()));
							}
						}
						if (nsubjDepEntry.getGov().getSlotEntry(0)
								.getResource().getResource()
								.equals("http://bk.sport.owl#happen")) {
							query.getWhere()
									.addTriplePattern(
											new Triple(
													nsubjDepEntry.getDep()
															.getSlotEntry(0)
															.getResource(),
													new IRI(
															"rdfs:subPropertyOf"),
													new IRI(
															"http://bk.sport.owl#happen")));
						}
					}
				}
				if (attrIndex >= 0 && prepIndex >= 0) {
					System.out.println("~~" + attrDepEntry);
					System.out.println(prepDepEntry);
					if (prepDepEntry.getSpecific().equals("about")) {
						query.getWhere().addTriplePattern(
								new Triple(attrDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("http://bk.sport.owl#about"),
										prepDepEntry.getDep().getSlotEntry(0)
												.getResource()));
						if (prepDepEntry.getGov().getValue().equals("news"))
							query.getWhere()
									.addTriplePattern(
											new Triple(attrDepEntry.getDep()
													.getSlotEntry(0)
													.getResource(), new IRI(
													"rdf", "type"), new IRI(
													"http://bk.sport.owl#News")));
					}
				}
				if (dobjIndex >= 0 && prepIndex >= 0) {
					if (prepDepEntry.getSpecific().equals("about")) {
						query.getWhere().addTriplePattern(
								new Triple(prepDepEntry.getGov()
										.getSlotEntry(0).getResource(),
										new IRI("http://bk.sport.owl#about"),
										prepDepEntry.getDep().getSlotEntry(0)
												.getResource()));
					}
				}
			} else if (nsubjpassIndex >= 0) {
				// TODO: Apply only for agent or prep, not both
				// need to fix to apply for all of agent and prep
				if (agentIndex >= 0) {
					if (agentDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (agentDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(agentDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(agentDepEntry
													.getDep().getValue()))));
						}
					} else if (agentDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(agentDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"), agentDepEntry
												.getDep().getSlotEntry(0)
												.getType()));
					}
					if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NNP")) {
						if (nsubjpassDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjpassDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjpassDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						if (nsubjpassDepEntry.getDep().getSlotEntry(0)
								.getType() != null)
							query.getWhere()
									.addTriplePattern(
											new Triple(nsubjpassDepEntry
													.getDep().getSlotEntry(0)
													.getResource(), new IRI(
													"rdf", "type"),
													nsubjpassDepEntry.getDep()
															.getSlotEntry(0)
															.getType()));
					}
					query.getWhere().addTriplePattern(
							new Triple(agentDepEntry.getDep().getSlotEntry(0)
									.getResource(), agentDepEntry.getGov()
									.getSlotEntry(0).getResource(),
									nsubjpassDepEntry.getDep().getSlotEntry(0)
											.getResource()));
				} else if (prepIndex >= 0 && prepDepEntry.getSpecific() != null) {
					if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NNP")) {
						if (nsubjpassDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjpassDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjpassDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(nsubjpassDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"),
										nsubjpassDepEntry.getDep()
												.getSlotEntry(0).getType()));
					}
					if (prepDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (prepDepEntry.getDep().getSlotEntry(0).getResource()
								.getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(prepDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(prepDepEntry
													.getDep().getValue()))));
						}
					} else if (prepDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(prepDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"), prepDepEntry
												.getDep().getSlotEntry(0)
												.getType()));
					}
					query.getWhere().addTriplePattern(
							new Triple(nsubjpassDepEntry.getDep()
									.getSlotEntry(0).getResource(),
									nsubjpassDepEntry.getGov().getSlotEntry(0)
											.getResource(), prepDepEntry
											.getDep().getSlotEntry(0)
											.getResource()));
				} else if (advmodIndex >= 0) {
					if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NNP")) {
						if (nsubjpassDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjpassDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjpassDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(nsubjpassDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"),
										nsubjpassDepEntry.getDep()
												.getSlotEntry(0).getType()));
					}
					if (advmodDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (advmodDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(advmodDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(advmodDepEntry
													.getDep().getValue()))));
						}
					} else if (advmodDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(advmodDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"), advmodDepEntry
												.getDep().getSlotEntry(0)
												.getType()));
					}
					query.getWhere().addTriplePattern(
							new Triple(nsubjpassDepEntry.getDep()
									.getSlotEntry(0).getResource(),
									nsubjpassDepEntry.getGov().getSlotEntry(0)
											.getResource(), advmodDepEntry
											.getDep().getSlotEntry(0)
											.getResource()));
				} else if (iobjIndex >= 0) {

				} else {
					if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(nsubjpassDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"),
										nsubjpassDepEntry.getDep()
												.getSlotEntry(0).getType()));
					}
				}
			} else {

			}

			// recognize returned object in SELECT query
			if (whPhraseTree != null) {
				for (int i = 0; i < lexList.size(); i++) {
					if (lexList.get(i).getTagName().startsWith("W")) {
						if (lexList.get(i).getTagName().equals("WDT")) {// which
							((SelectQuery) query).addVar((Var) lexList
									.get(i + 1).getSlotEntry(0).getResource());
							if (nlQuestion.toLowerCase().contains("news")) {
								Var url = new Var();
								((SelectQuery) query).addVar(url);
								query.getWhere().addTriplePattern(
										new Triple((Var) lexList.get(i + 1)
												.getSlotEntry(0).getResource(),
												new IRI(query, "bksport",
														"hasURL"), url));
							}
						} else if (lexList.get(i).getTagName().equals("WP")) {// what,
																				// who
							((SelectQuery) query).addVar((Var) lexList.get(i)
									.getSlotEntry(0).getResource());
							if (expectedResult == PERSON_ORGANIZATION_RESULT) {
								Var label = new Var();
								((SelectQuery) query).addVar(label);
								query.getWhere()
										.addTriplePattern(
												new Triple((Var) lexList.get(i)
														.getSlotEntry(0)
														.getResource(),
														new IRI(query, "rdfs",
																"label"), label));
							}
							if (expectedResult == DESCRIPTION_RESULT) {
								Var abstr = new Var();
								((SelectQuery) query).addVar(abstr);
								query.getWhere().addTriplePattern(
										new Triple((Var) lexList.get(i)
												.getSlotEntry(0).getResource(),
												new IRI(query, "bksport",
														"hasAbstract"), abstr));
							}
							// Var abstr = new Var();
							// query.getWhere().addTriplePattern(new Triple(
							// (Var)lexList.get(i).getSlotEntry(0).getSlot()
							// , new IRI(query, "bksport", "hasAbstract")
							// , abstr
							// ));
							// ((SelectQuery)query).addVar(abstr);
							// if(lexList.get(i).getValue().toLowerCase().equals("who")){
							// query.getWhere().addTriplePattern(new Triple(
							// (Var)lexList.get(i).getSlotEntry(0).getSlot()
							// , new IRI("rdf", "type")
							// , new IRI(query, "bksport", "Person")));
							// }
						} else if (lexList.get(i).getTagName().equals("WP$")) {// whose

						} else if (lexList.get(i).getTagName().equals("WRB")) {// where,
																				// when,
																				// how
							if (lexList.get(i).getValue().equals("where")) {
								Var label = new Var();
								if (lexList.get(i).getNumOfSlotEntry() > 0) {
									query.getWhere().addTriplePattern(
											new Triple((Var) lexList.get(i)
													.getSlotEntry(0)
													.getResource(), new IRI(
													query, "rdfs", "label"),
													label));
									query.getWhere().addTriplePattern(
											new Triple((Var) lexList.get(i)
													.getSlotEntry(0)
													.getResource(), new IRI(
													query, "rdf", "type"),
													new IRI(query, "bksport",
															"Location")));
								}
								((SelectQuery) query).addVar(label);
							} else if (lexList.get(i).getValue().toLowerCase()
									.equals("how")) {
								// TODO: implement SPARQL 1.1
								if (lexList.get(i + 1).getValue().toLowerCase()
										.equals("many")) {
									((SelectQuery) query).addVar(new Var(
											"COUNT("
													+ lexList.get(i + 2)
															.getSlotEntry(0)
															.getResource()
													+ ")"));
								}
							}
						}
					}
				}
				// for(int i = 0; i < depList.size(); i++){
				// DepEntry depEntry = depList.get(i);
				// if(depEntry.getType().equals("det") &&
				// depEntry.getDep().getTagName().equals("WDT")){
				// ((SelectQuery)query).addVar((Var)depEntry.getGov().getSlotEntry(0).getSlot());
				// }
				// if(depEntry.getType().equals("attr") &&
				// depEntry.getDep().getTagName().startsWith("W")){
				// ((SelectQuery)query).addVar((Var)depEntry.getDep().getSlotEntry(0).getSlot());
				// }
				// }
				// selectVar = new Var("x");
				// ((SelectQuery)query).addVar(selectVar);
			} else 
			{// command
				// detect focus object

				if (prepIndex >= 0) 
				{
					if (prepDepEntry.getDep().getTagName().startsWith("NNP")) 
					{
						if (prepDepEntry.getDep().getSlotEntry(0).getResource()
								.getClass() == Var.class) 
						{
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(prepDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(prepDepEntry
													.getDep().getValue()))));
						}
					} else if (prepDepEntry.getDep().getTagName()
							.startsWith("NN")) 
					{
						query.getWhere().addTriplePattern(
								new Triple(prepDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"), prepDepEntry
												.getDep().getSlotEntry(0)
												.getType()));
					}
					// query.getWhere().addTriplePattern(
					// new
					// Triple(prepDepEntry.getGov().getSlotEntry(0).getResource(),
					// ResourceRecognizer
					// .getDomainResourceEntrySet(prepDepEntry.getSpecific())
					// .iterator().next().getResource(), prepDepEntry.getDep()
					// .getSlotEntry(0).getResource()));
				}
				// if (prepDepEntry.getGov().getTagName().startsWith("NN")) {
				// ((SelectQuery) query).addVar((Var) prepDepEntry.getGov()
				// .getSlotEntry(0).getResource());
				// query.getWhere().addTriplePattern(
				// new
				// Triple(prepDepEntry.getGov().getSlotEntry(0).getResource(),
				// new IRI("rdf", "type"), prepDepEntry.getGov().getSlotEntry(0)
				// .getType()));
				// }
			}
		} else 
		{
			query = new SelectQuery();
			sqClauseTree = clauseTree;
			for (int i = 0; i < lexList.size(); i++) 
			{
				if (lexList.get(i).getTagName().startsWith("NNP")) 
				{

				} else if (lexList.get(i).getTagName().startsWith("NN")) 
				{
					System.out.println("::::" + lexList.get(i).getValue());
					((SelectQuery) query).addVar((Var) lexList.get(i)
							.getSlotEntry(0).getResource());
					query.getWhere().addTriplePattern(
							new Triple(lexList.get(i).getSlotEntry(0)
									.getResource(), lexList.get(i)
									.getSlotEntry(0).getType()));
					break;
				}
			}
			int nsubjIndex = -1;
			int nsubjpassIndex = -1;
			int dobjIndex = -1;
			int pobjIndex = -1;
			int iobjIndex = -1;
			int attrIndex = -1;
			int agentIndex = -1;
			int prepIndex = -1;
			int advmodIndex = -1;
			DepEntry nsubjDepEntry = null;
			DepEntry nsubjpassDepEntry = null;
			DepEntry dobjDepEntry = null;
			DepEntry pobjDepEntry = null;
			DepEntry iobjDepEntry = null;
			DepEntry attrDepEntry = null;
			DepEntry agentDepEntry = null;
			DepEntry prepDepEntry = null;
			DepEntry advmodDepEntry = null;
			depList = new ArrayList<DepEntry>();
			for (int i = 0; i < typedDepList.size(); i++) {
				TypedDependency typedDep = typedDepList.get(i);
				TreeGraphNode dep = typedDep.dep();
				TreeGraphNode gov = typedDep.gov();
				if (DepEntry.isSupportType(typedDep.reln().getShortName())) {
					DepEntry depEntry = new DepEntry();
					depEntry.setType(typedDep.reln().getShortName());
					if (typedDep.reln().getSpecific() != null
							&& !typedDep.reln().getSpecific().equals("null")) {
						depEntry.setSpecific(typedDep.reln().getSpecific());
					}
					depEntry.setDep(lexList.get(realPossitionList.get(dep
							.index() - 1)));
					depEntry.setGov(lexList.get(realPossitionList.get(gov
							.index() - 1)));
					depList.add(depEntry);
					if (depEntry.getType().equals("nsubj")) {
						nsubjIndex = depList.size() - 1;
						nsubjDepEntry = depEntry;
					}
					if (depEntry.getType().equals("nsubjpass")) {
						nsubjpassIndex = depList.size() - 1;
						nsubjpassDepEntry = depEntry;
					}
					if (depEntry.getType().equals("dobj")) {
						dobjIndex = depList.size() - 1;
						dobjDepEntry = depEntry;
					}
					if (depEntry.getType().equals("pobj")) {
						pobjIndex = depList.size() - 1;
						pobjDepEntry = depEntry;
					}
					if (depEntry.getType().equals("iobj")) {
						iobjIndex = depList.size() - 1;
						iobjDepEntry = depEntry;
					}
					if (depEntry.getType().equals("attr")) {
						attrIndex = depList.size() - 1;
						attrDepEntry = depEntry;
					}
					if (depEntry.getType().equals("agent")) {
						agentIndex = depList.size() - 1;
						agentDepEntry = depEntry;
					}
					if (depEntry.getType().equals("prep")) {
						prepIndex = depList.size() - 1;
						prepDepEntry = depEntry;
					}
					if (depEntry.getType().equals("advmod")) {
						advmodIndex = depList.size() - 1;
						advmodDepEntry = depEntry;
					}
				}
			}

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
						if (depEntry.getSpecific().equals("about")) 
						{
							if (depEntry.getDep().getTagName().startsWith("NNP")) 
							{
								if (depEntry.getDep().getSlotEntry(0).getResource().getClass() == Var.class) 
								{
									Var label = new Var();
									query.getWhere().addTriplePattern(
											new Triple(depEntry.getDep()
													.getSlotEntry(0)
													.getResource(), new IRI(
													"rdfs", "label"), label));
									query.getWhere().addFilter(
											new Constraint(new EqualExpression(
													new StrFunction(label),
													new RDFLiteral(depEntry
															.getDep()
															.getValue()))));
								}
							} else if (depEntry.getDep().getTagName()
									.startsWith("NN")
									&& depEntry.getDep().getSlotEntry(0)
											.getType() != null
									&& depEntry.getDep().getSlotEntry(0)
											.getType()
											.equals(IRI.OWL_CLASS_IRI)) {
								query.getWhere().addTriplePattern(
										new Triple(depEntry.getDep()
												.getSlotEntry(0).getResource(),
												new IRI("rdf", "type"),
												depEntry.getDep()
														.getSlotEntry(0)
														.getType()));
							}
							query.getWhere().addTriplePattern(
									new Triple(depEntry.getGov()
											.getSlotEntry(0).getResource(),
											new IRI(NSUtil.bksport + "about"),
											depEntry.getDep().getSlotEntry(0)
													.getResource()));
						}
					}
					
					
					if (depEntry.getType().equals("pobj")) {
						if (depEntry.getGov().getValue().equals("about")) {
							Var nVar = new Var();
							((SelectQuery) query).addVar(nVar);
							if (depEntry.getDep().getTagName()
									.startsWith("NNP")) {
								if (depEntry.getDep().getSlotEntry(0)
										.getResource().getClass() == Var.class) {
									Var label = new Var();
									query.getWhere().addTriplePattern(
											new Triple(depEntry.getDep()
													.getSlotEntry(0)
													.getResource(), new IRI(
													"rdfs", "label"), label));
									query.getWhere().addFilter(
											new Constraint(new EqualExpression(
													new StrFunction(label),
													new RDFLiteral(depEntry
															.getDep()
															.getValue()))));
								}
							} else if (depEntry.getDep().getTagName().startsWith("NN")
									&& depEntry.getDep().getSlotEntry(0).getType() != null
									&& depEntry.getDep().getSlotEntry(0).getType().equals(IRI.OWL_CLASS_IRI)) 
							{
								query.getWhere().addTriplePattern(
										new Triple(depEntry.getDep()
												.getSlotEntry(0).getResource(),
												new IRI("rdf", "type"),
												depEntry.getDep()
														.getSlotEntry(0)
														.getType()));
							}
							
							query.getWhere().addTriplePattern(
									new Triple(
											nVar, 
											new IRI(NSUtil.bksport+ "about"), 
											depEntry.getDep().getSlotEntry(0).getResource()));
						}
					}
				}
			}

			if (nsubjIndex >= 0) {
				if (dobjIndex >= 0) {
					if (nsubjDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (nsubjDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						if (nsubjDepEntry.getDep().getSlotEntry(0).getType() != null)
							query.getWhere()
									.addTriplePattern(
											new Triple(nsubjDepEntry.getDep()
													.getSlotEntry(0)
													.getResource(), new IRI(
													"rdf", "type"),
													nsubjDepEntry.getDep()
															.getSlotEntry(0)
															.getType()));
					}
					if (dobjDepEntry.getDep().getTagName().startsWith("NNP")) {

						if (dobjDepEntry.getDep().getSlotEntry(0).getResource()
								.getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(dobjDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(dobjDepEntry
													.getDep().getValue()))));
						}
					} else if (dobjDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						if (dobjDepEntry.getDep().getSlotEntry(0).getType() != null)
							query.getWhere()
									.addTriplePattern(
											new Triple(dobjDepEntry.getDep()
													.getSlotEntry(0)
													.getResource(), new IRI(
													"rdf", "type"),
													dobjDepEntry.getDep()
															.getSlotEntry(0)
															.getType()));
					}
					query.getWhere().addTriplePattern(
							new Triple(nsubjDepEntry.getDep().getSlotEntry(0)
									.getResource(), nsubjDepEntry.getGov()
									.getSlotEntry(0).getResource(),
									dobjDepEntry.getDep().getSlotEntry(0)
											.getResource()));
				} else if (attrIndex >= 0) {
					if (attrDepEntry.getGov().getValue().equals("is")
							|| attrDepEntry.getGov().getValue().equals("was")
							|| attrDepEntry.getGov().getValue().equals("were")
							|| attrDepEntry.getGov().getValue().equals("'s")) {
						if (nsubjDepEntry.getDep().getTagName()
								.startsWith("NNP")) {
							if (nsubjDepEntry.getDep().getSlotEntry(0)
									.getResource().getClass() == Var.class) {
								Var label = new Var();
								query.getWhere()
										.addTriplePattern(
												new Triple(
														attrDepEntry
																.getDep()
																.getSlotEntry(0)
																.getResource(),
														new IRI("rdfs", "label"),
														label));
								query.getWhere()
										.addFilter(
												new Constraint(
														new EqualExpression(
																new StrFunction(
																		label),
																new RDFLiteral(
																		nsubjDepEntry
																				.getDep()
																				.getValue()))));
							}
							query.getWhere().addTriplePattern(
									new Triple(nsubjDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI(query, "bksport",
													"hasAbstract"),
											attrDepEntry.getDep()
													.getSlotEntry(0)
													.getResource()));
						} else if (nsubjDepEntry.getDep().getTagName()
								.startsWith("NN")) {
							// TODO: who is manager --> hasAbstract
							// What is manager --> hasAbstract
							// Who is manager of Barcenola --> find person
							// What is team of Lionel Messi --> find team
							if (prepIndex >= 0) {
								// TODO: null pointer problem when
								// "who is manager of MU"
								// without concept manager!!!
								query.getWhere().addTriplePattern(
										new Triple(attrDepEntry.getDep()
												.getSlotEntry(0).getResource(),
												new IRI(query, "rdf", "type"),
												nsubjDepEntry.getDep()
														.getSlotEntry(0)
														.getType()));
								// TODO: thi���u ph���n �����nh ngh��a v���
								// att...Dep
							} else {
								query.getWhere().addTriplePattern(
										new Triple(nsubjDepEntry.getDep()
												.getSlotEntry(0).getResource(),
												new IRI(query, "bksport",
														"hasAbstract"),
												attrDepEntry.getDep()
														.getSlotEntry(0)
														.getResource()));
							}
							System.out.println("*******"
									+ attrDepEntry.getGov().getValue());
							// query.getWhere().addTriplePattern(
							// new Triple(nsubjDepEntry.getDep().getSlotEntry(0)
							// .getResourceEntry().getResource(), new IRI(query,
							// "bksport", "hasAbstract"), attrDepEntry.getDep()
							// .getSlotEntry(0).getSlot()));
						}
						// TODO: maybe redundant
						// query.getWhere().addTriplePattern(
						// new
						// Triple(nsubjDepEntry.getDep().getSlotEntry(0).getSlot(),
						// nsubjDepEntry.getGov().getSlotEntry(0).getSlot(),
						// attrDepEntry.getDep().getSlotEntry(0).getSlot()));
					}
				} else if (prepIndex >= 0 && prepDepEntry.getSpecific() != null) {
					if (nsubjDepEntry.getDep().getTagName().startsWith("NNP")) {
						System.out.println("==1");
						if (nsubjDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						System.out.println("==2");
						if (nsubjDepEntry.getDep().getSlotEntry(0).getType() != null)
							query.getWhere()
									.addTriplePattern(
											new Triple(nsubjDepEntry.getDep()
													.getSlotEntry(0)
													.getResource(), new IRI(
													"rdf", "type"),
													nsubjDepEntry.getDep()
															.getSlotEntry(0)
															.getType()));
					}
					if (prepDepEntry.getDep().getTagName().startsWith("NNP")) {
						System.out.println("==3");
						if (prepDepEntry.getDep().getSlotEntry(0).getResource()
								.getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(prepDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(prepDepEntry
													.getDep().getValue()))));
						}
					} else if (prepDepEntry.getDep().getTagName()
							.startsWith("NN")
							&& prepDepEntry.getDep().getSlotEntry(0).getType()
									.equals(IRI.OWL_CLASS_IRI)) {
						System.out.println("==4");
						query.getWhere().addTriplePattern(
								new Triple(prepDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"), prepDepEntry
												.getDep().getSlotEntry(0)
												.getType()));
					}
					query.getWhere().addTriplePattern(
							new Triple(nsubjDepEntry.getDep().getSlotEntry(0)
									.getResource(), nsubjDepEntry.getGov()
									.getSlotEntry(0).getResource(),
									prepDepEntry.getDep().getSlotEntry(0)
											.getResource()));
				}
			} else if (nsubjpassIndex >= 0) {
				// TODO: Apply only for agent or prep, not both
				// need to fix to apply for all of agent and prep
				if (agentIndex >= 0) {
					if (agentDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (agentDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(agentDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(agentDepEntry
													.getDep().getValue()))));
						}
					} else if (agentDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(agentDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"), agentDepEntry
												.getDep().getSlotEntry(0)
												.getType()));
					}
					if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NNP")) {
						if (nsubjpassDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjpassDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjpassDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						if (nsubjpassDepEntry.getDep().getSlotEntry(0)
								.getType() != null)
							query.getWhere()
									.addTriplePattern(
											new Triple(nsubjpassDepEntry
													.getDep().getSlotEntry(0)
													.getResource(), new IRI(
													"rdf", "type"),
													nsubjpassDepEntry.getDep()
															.getSlotEntry(0)
															.getType()));
					}
					query.getWhere().addTriplePattern(
							new Triple(agentDepEntry.getDep().getSlotEntry(0)
									.getResource(), agentDepEntry.getGov()
									.getSlotEntry(0).getResource(),
									nsubjpassDepEntry.getDep().getSlotEntry(0)
											.getResource()));
				} else if (prepIndex >= 0 && prepDepEntry.getSpecific() != null) {
					if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NNP")) {
						if (nsubjpassDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjpassDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjpassDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(nsubjpassDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"),
										nsubjpassDepEntry.getDep()
												.getSlotEntry(0).getType()));
					}
					if (prepDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (prepDepEntry.getDep().getSlotEntry(0).getResource()
								.getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(prepDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(prepDepEntry
													.getDep().getValue()))));
						}
					} else if (prepDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(prepDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"), prepDepEntry
												.getDep().getSlotEntry(0)
												.getType()));
					}
					query.getWhere().addTriplePattern(
							new Triple(nsubjpassDepEntry.getDep()
									.getSlotEntry(0).getResource(),
									nsubjpassDepEntry.getGov().getSlotEntry(0)
											.getResource(), prepDepEntry
											.getDep().getSlotEntry(0)
											.getResource()));
				} else if (advmodIndex >= 0) {
					if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NNP")) {
						if (nsubjpassDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(nsubjpassDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(nsubjpassDepEntry
													.getDep().getValue()))));
						}
					} else if (nsubjpassDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(nsubjpassDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"),
										nsubjpassDepEntry.getDep()
												.getSlotEntry(0).getType()));
					}
					if (advmodDepEntry.getDep().getTagName().startsWith("NNP")) {
						if (advmodDepEntry.getDep().getSlotEntry(0)
								.getResource().getClass() == Var.class) {
							Var label = new Var();
							query.getWhere().addTriplePattern(
									new Triple(advmodDepEntry.getDep()
											.getSlotEntry(0).getResource(),
											new IRI("rdfs", "label"), label));
							query.getWhere().addFilter(
									new Constraint(new EqualExpression(
											new StrFunction(label),
											new RDFLiteral(advmodDepEntry
													.getDep().getValue()))));
						}
					} else if (advmodDepEntry.getDep().getTagName()
							.startsWith("NN")) {
						query.getWhere().addTriplePattern(
								new Triple(advmodDepEntry.getDep()
										.getSlotEntry(0).getResource(),
										new IRI("rdf", "type"), advmodDepEntry
												.getDep().getSlotEntry(0)
												.getType()));
					}
					query.getWhere().addTriplePattern(
							new Triple(nsubjpassDepEntry.getDep()
									.getSlotEntry(0).getResource(),
									nsubjpassDepEntry.getGov().getSlotEntry(0)
											.getResource(), advmodDepEntry
											.getDep().getSlotEntry(0)
											.getResource()));
				} else if (iobjIndex >= 0) {

				}
			} else {

			}
		}

		
		

	    // ==========================================================================
	    // TODO: Refactoring code to new algorithm
	    // newOrQuery
	    // ==========================================================================
	    ArrayList<LexicalEntry> andList = new ArrayList<LexicalEntry>();
	    System.out.println("TTT typedDepList: " + typedDepList);
	    System.out.println("TTT depList 1: " + depList);
	    
	    if(depList==null)
	    {
	    	return null;
	    }
	    
	    for (int i = 0; i < typedDepList.size(); i++)
	    {
	      TypedDependency typedDep = typedDepList.get(i);
	      TreeGraphNode dep = typedDep.dep();
	      TreeGraphNode gov = typedDep.gov();
	      System.out.println("TTT typedDep.reln: " + typedDep.reln() + " - shortName: " + typedDep.reln().getShortName());
	      if (DepEntry.isSupportType(typedDep.reln().getShortName())) 
	      {
	        DepEntry depEntry = new DepEntry();
	        depEntry.setType(typedDep.reln().getShortName());
	        if (typedDep.reln().getSpecific() != null && !typedDep.reln().getSpecific().equals("null")) 
	        {
	          depEntry.setSpecific(typedDep.reln().getSpecific());
	        }
	        depEntry.setDep(lexList.get(realPossitionList.get(dep.index() - 1)));
	        depEntry.setGov(lexList.get(realPossitionList.get(gov.index() - 1)));
	        
	        //depList.add(depEntry);	// TTT bi lap lai
	        
	        // and // TTT chua dung den andList
	        if (depEntry.getType().equals("conj")) 
	        {
	          // TODO
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
	    

	    if (andList.size() > 0) 
	    {
	      System.out.println("Exist and list: ");
	      for (LexicalEntry lexEntry : andList) 
	      {
	        System.out.print(lexEntry.getValue() + "\t");
	      }
	      System.out.println();
	    }

	    
	    // TTT
	    int nsubjIndex = -1;
	    int nsubjpassIndex = -1;
	    int typedOtherIndex = -1;
	    int dobjIndex = -1;
	    int attrIndex = -1;
	    int prepIndex = -1;
	    int advmodIndex = -1;
	    int depIndex = -1;
	    
	    ArrayList<DepEntry> sp = new ArrayList<DepEntry>();	// subject-predicate
	    ArrayList<DepEntry> po = new ArrayList<DepEntry>();	// predicate-object
	    
	    // Noi nsubject (S V) & dobject (V O) sinh --> pattern ?s ?v ?o
	    
	    System.out.println("TTT depList: " + depList);
	    
	    for (int i = 0; i < depList.size(); i++) 
	    {
	      DepEntry depEntryBefore = depList.get(i);
	      if(depEntryBefore.getType().equals("nsubj"))
	      {
	    	  nsubjIndex++;
	      } else if (depEntryBefore.getType().equals("nsubjpass"))
	      {
	    	  nsubjpassIndex++;
	      }
	            
	      for (int j = i + 1; j < depList.size(); j++) // Duyet qua tung cap Dependence (A, B)
	      {
	        DepEntry depEntryAfter = depList.get(j);
	                
	        
	        // Doan nay de noi (A, B) && (B, C) ---> (A, B, C)
	        if (depEntryBefore.getDep() == depEntryAfter.getDep()) {
	        	// do nothing
	        }
	        if (depEntryBefore.getDep() == depEntryAfter.getGov()) {
	        	// do nothing
	        }        
	        
	        if (depEntryBefore.getGov() == depEntryAfter.getDep()) {
	        	// do nothing
	        }
	        
	        if (depEntryBefore.getGov() == depEntryAfter.getGov()) 
	        {
	          if (depEntryBefore.getType().equals("nsubj")) 
	          {
	            if (depEntryAfter.getType().equals("dobj")
	                || depEntryAfter.getType().equals("prep")                
	                || depEntryAfter.getType().equals("advmod")
	                || depEntryAfter.getType().equals("attr")
	                || depEntryAfter.getType().equals("dep")) 
	            {
	            	typedOtherIndex++;
	              sp.add(depEntryBefore);
	              po.add(depEntryAfter);
	            }
	          }
	          
	          if (depEntryAfter.getType().equals("nsubj")) 
	          {
	            if (depEntryBefore.getType().equals("dobj")
	                || depEntryBefore.getType().equals("prep")
	                || depEntryBefore.getType().equals("advmod")
	                || depEntryBefore.getType().equals("attr")
	                || depEntryBefore.getType().equals("dep")) 
	            {
	            	typedOtherIndex++;
	              sp.add(depEntryAfter);
	              po.add(depEntryBefore);
	            }
	          }
	          
	          if (depEntryBefore.getType().equals("nsubjpass")) 
	          {
	            if (depEntryAfter.getType().equals("agent")
	                || depEntryAfter.getType().equals("prep")
	                || depEntryAfter.getType().equals("advmod")
	                || depEntryAfter.getType().equals("attr")
	                || depEntryAfter.getType().equals("dep")) 
	            {
	            	typedOtherIndex++;
	              sp.add(depEntryAfter);
	              po.add(depEntryBefore);
	            }            
	          }
	          
	          if (depEntryAfter.getType().equals("nsubjpass")) 
	          {
	            if (depEntryBefore.getType().equals("agent")
	                || depEntryBefore.getType().equals("prep")
	                || depEntryBefore.getType().equals("advmod")
	                || depEntryBefore.getType().equals("attr")
	                || depEntryBefore.getType().equals("dep")) 
	            {
	            	typedOtherIndex++;
	              sp.add(depEntryBefore);
	              po.add(depEntryAfter);
	            }
	          }
	        }
	      }
	    }
	    
	    
	    System.out.println("TTT sp: " + sp);
	    System.out.println(("TTT po: " + po));
	    
	    
	Query newOrQuery = new SelectQuery();
	    
	System.out.println("TTT clauseTag: " + clauseTag);
	    
	    if (clauseTag.equals("S"))
	    {
	      newOrQuery = new SelectQuery();
	    } else if (clauseTag.equals("SBAR")) 
	    {
	      newOrQuery = new SelectQuery();
	    } else if (clauseTag.equals("SBARQ")) 
	    {
	      newOrQuery = new SelectQuery();
	      whPhraseTree = clauseTree.children()[0];
	      sqClauseTree = clauseTree.children()[1];
	    } else if (clauseTag.equals("SINV")) 
	    {
	    	
	    } else if (clauseTag.equals("SQ")) 
	    {
	      newOrQuery = new AskQuery();
	      sqClauseTree = clauseTree;
	    } else {
	      newOrQuery = new SelectQuery();
	    }    
	    
	    if (newOrQuery != null) 
	    {
	      newOrQuery.addPrefix("bksport", new IRI(NSUtil.bksport));
	      newOrQuery.addPrefix("owl", IRI.OWL_IRI);
	      newOrQuery.addPrefix("rdf", IRI.RDF_IRI);
	      newOrQuery.addPrefix("rdfs", IRI.RDFS_IRI);
	      //newOrQuery.setWhere(newWhereOrClause);
	    }
	    
	    GroupGraphPattern newWhereOrClause = new GroupGraphPattern();
	    Set<DepEntry> labeledOrSet = new HashSet<DepEntry>();

	    boolean doOr1 = false;
	    boolean doOr2 = false;
	    boolean doOr3 = false;
	    boolean doOr4 = false;
	    String DESCRIPTION_RESULT = "description_result";
	    String ENTITY_RESULT = "entity_result";
	    String PERSON_ORGANIZATION_RESULT = "person_organization_result";
	    String LOCATION_RESULT = "location_result";
	    String NUMBER_RESULT = "number_result";
	    String expectedResult = null;
	    
	    //System.out.println("TTT andList: " + andList);

	    for (int i = 0; i < sp.size(); i++) 
	    {
	      DepEntry depEntryBefore = sp.get(i);
	      DepEntry depEntryAfter = po.get(i);
	      if (depEntryBefore.getDep().getTagName().startsWith("NNP") && !(doOr1))
	      {
	        if (depEntryBefore.getDep().getSlotEntry(0).getResource().getClass() == Var.class
	            && !labeledOrSet.contains(depEntryBefore))
	        {
	        	System.out.println(true + "1");
	          labeledOrSet.add(depEntryBefore);
	          Var label = new Var();
	          newWhereOrClause.addTriplePattern(new Triple(
	        		  depEntryBefore.getDep().getSlotEntry(0).getResource(), 
	        		  new IRI("rdfs", "label"), 
	        		  label));
	          newWhereOrClause.addFilter(new Constraint(new EqualExpression(new StrFunction(label), 
	        		  new RDFLiteral(depEntryBefore.getDep().getValue()))));
	        }
	      } else if (depEntryBefore.getDep().getTagName().startsWith("NN") && !(doOr2))
	      {
	        if (depEntryBefore.getDep().getSlotEntry(0).getType() != null) 
	        {
	        	System.out.println(true + "2");
	        	newWhereOrClause.addTriplePattern(new Triple(
	        		  depEntryBefore.getDep().getSlotEntry(0).getResource(), 
	        		  new IRI("rdf", "type"),
	        		  depEntryBefore.getDep().getSlotEntry(0).getType()));
	        }
	      }
	      
	      if (depEntryAfter.getDep().getTagName().startsWith("NNP") && !(doOr3)) 
	      {
	        if (depEntryAfter.getDep().getSlotEntry(0).getResource().getClass() == Var.class
	            && !labeledOrSet.contains(depEntryAfter))
	        {
	        	 System.out.println(true + "3");
	          labeledOrSet.add(depEntryAfter);
	          Var label = new Var();
	          newWhereOrClause.addTriplePattern(new Triple(
	        		  depEntryAfter.getDep().getSlotEntry(0).getResource(), 
	        		  new IRI("rdfs", "label"), 
	        		  label));
	          newWhereOrClause.addFilter(new Constraint(new EqualExpression(new StrFunction(
	              label), new RDFLiteral(depEntryAfter.getDep().getValue()))));
	        }
	        
	      } else if (depEntryAfter.getDep().getTagName().startsWith("NN") && !(doOr4)) 
	      {        
	        if (depEntryAfter.getDep().getSlotEntry(0).getType() != null) 
	        {
	        	System.out.println(true + "4");
	        	newWhereOrClause.addTriplePattern(new Triple(
	        		  depEntryAfter.getDep().getSlotEntry(0).getResource(), 
	        		  new IRI("rdf", "type"),
	        		  depEntryAfter.getDep().getSlotEntry(0).getType()));
	        }

	      }

	      // Tuong tu vs NNP: xu ly cau dang: Was Barca and Chelsea defeated by Manchester?
	      if (depEntryBefore.getGov().getValue().equals("is")
	              || depEntryBefore.getGov().getValue().equals("was")
	              || depEntryBefore.getGov().getValue().equals("were")
	              || depEntryBefore.getGov().getValue().equals("'s")
	              || depEntryBefore.getGov().getValue().equals("are")
	              || depEntryBefore.getGov().getValue().equals("'re"))
	      {
	      	newWhereOrClause.addTriplePattern(new Triple(
	          		  depEntryBefore.getDep().getSlotEntry(0).getResource(),
	          		  new IRI(newOrQuery, "bksport", "hasAbstract"), 
	          		  depEntryAfter.getDep().getSlotEntry(0).getResource()));
	      	
	      	expectedResult = null;	// khong them abstract nua.
	      }
	      else
	      {
	      	newWhereOrClause.addTriplePattern(new Triple(
	          		  depEntryBefore.getDep().getSlotEntry(0).getResource(),
	          		  depEntryBefore.getGov().getSlotEntry(0).getResource(), 
	          		  depEntryAfter.getDep().getSlotEntry(0).getResource()));
	      }
	    }      
	    
	    // Hau xu ly,
	    // Cau co dang: who scored yesterday?, Did Messi score yesterday?, Did Messi score in the match? (cau nay van dang bi loi)
	    // Was Barca defeated.
	    System.out.println("nsubj: " + nsubjIndex + " - pass: " + nsubjpassIndex + " - other: " + typedOtherIndex);
	    if(nsubjIndex>=0 && typedOtherIndex<0)
	    {
	    	for (int i = 0; i < depList.size(); i++) 
	        {
	          DepEntry temp = depList.get(i);
	          if(temp.getType().equals("nsubj"))
	          {
	        	  Var object = new Var();
	        	  newWhereOrClause.addTriplePattern(new Triple(
	        			  temp.getDep().getSlotEntry(0).getResource(),
	        			  temp.getGov().getSlotEntry(0).getResource(),
	        			  object));
	          }
	        }
	    } else if(nsubjpassIndex>=0 && typedOtherIndex<0)
	    {
	    	for (int i = 0; i < depList.size(); i++) 
	        {
	          DepEntry temp = depList.get(i);
	          if(temp.getType().equals("nsubjpass"))
	          {
	        	  Var object = new Var();
	        	  newWhereOrClause.addTriplePattern(new Triple(
	        			  object,
	        			  temp.getGov().getSlotEntry(0).getResource(),
	        			  temp.getDep().getSlotEntry(0).getResource()));
	          }
	        }
	    }
	    
	    // TTT
	    if(newOrQuery != null)
	    {
	    	newOrQuery.setWhere(newWhereOrClause);
	    }

	    if ((clauseTag.equals("S") || clauseTag.equals("SBAR")
	        || clauseTag.equals("SBARQ") || clauseTag.equals("SQ"))
	        && newOrQuery != null) 
	    {     
	      if (whPhraseTree != null) 
	      {
	        List<Tree> sb = whPhraseTree.getLeaves();
	        for (int i = 0; i < sb.size(); i++) 
	        {
	          String value = sb.get(i).value().toLowerCase();
	          if (value.startsWith("wh")) 
	          {
	            if (value.equals("where")) 
	            {
	              expectedResult = LOCATION_RESULT;
	            } else if (value.equals("when")) 
	            {
	              expectedResult = NUMBER_RESULT;
	            } else if (value.equals("whom")) 
	            {
	              expectedResult = PERSON_ORGANIZATION_RESULT;
	            } else if (value.equals("who")) 
	            {
	            	// TTT
	            	expectedResult = PERSON_ORGANIZATION_RESULT;
	            } else if (value.equals("which")) {

	            } else if (value.equals("what")) {
	            	
	            }
	          }
	        }
	      }

	      // recognize returned object in SELECT query
	      if (whPhraseTree != null) 
	      {
	        for (int i = 0; i < lexList.size(); i++) 
	        {
	          if (lexList.get(i).getTagName().startsWith("W")) 
	          {
	            if (lexList.get(i).getTagName().equals("WDT")) 
	            {// which
	              ((SelectQuery) newOrQuery).addVar((Var) lexList.get(i + 1).getSlotEntry(0).getResource());
	              if (nlQuestion.toLowerCase().contains("news")) 
	              {
	                Var url = new Var();
	                ((SelectQuery) newOrQuery).addVar(url);
	                newOrQuery.getWhere().addTriplePattern(
	                    new Triple(
	                    		(Var) lexList.get(i + 1).getSlotEntry(0).getResource(),
	                    		new IRI(newOrQuery, "bksport", "hasURL"), 
	                    		url));
	              }
	            } else if (lexList.get(i).getTagName().equals("WP")) 
	            {// what, who
	            	
	              ((SelectQuery) newOrQuery).addVar((Var) lexList.get(i).getSlotEntry(0).getResource());
	              
	              if (expectedResult == PERSON_ORGANIZATION_RESULT) 
	              {
	                Var label = new Var();
	                ((SelectQuery) newOrQuery).addVar(label);
	                newOrQuery.getWhere().addTriplePattern(
	                    new Triple(
	                    		(Var) lexList.get(i).getSlotEntry(0).getResource(), 
	                    		new IRI(newOrQuery, "rdfs", "label"),
	                    		label));
	              }
	              
	              if (expectedResult == DESCRIPTION_RESULT) 
	              {
	                Var abstr = new Var();
	                
	                ((SelectQuery) newOrQuery).addVar(abstr);
	                
	                newOrQuery.getWhere().addTriplePattern(
	                    new Triple(
	                    		(Var) lexList.get(i).getSlotEntry(0).getResource(), 
	                    		new IRI(newOrQuery, "bksport", "hasAbstract"), 
	                    		abstr));
	              }
	            } else if (lexList.get(i).getTagName().equals("WP$")) 
	            {// whose

	            } else if (lexList.get(i).getTagName().equals("WRB")) 
	            {
	              // where,
	              //when, how
	              if (lexList.get(i).getValue().equals("where")) 
	              {
	                Var label = new Var();
	                if (lexList.get(i).getNumOfSlotEntry() > 0) // TTT getNumOfSlotEntry ??? 
	                {
	                  newOrQuery.getWhere().addTriplePattern(
	                      new Triple(
	                    		  (Var) lexList.get(i).getSlotEntry(0).getResource(), 
	                    		  new IRI(newOrQuery, "rdfs", "label"),
	                    		  label));
	                  
	                  newOrQuery.getWhere().addTriplePattern(
	                      new Triple(
	                    		  (Var) lexList.get(i).getSlotEntry(0).getResource(), 
	                    		  new IRI(newOrQuery, "rdf", "type"),
	                    		  new IRI(newOrQuery, "bksport", "Location")));
	                }
	                
	                ((SelectQuery) newOrQuery).addVar(label);
	                
	              } else if (lexList.get(i).getValue().toLowerCase().equals("how")) 
	              {
	                // TODO: implement SPARQL 1.1
	                if (lexList.get(i + 1).getValue().toLowerCase().equals("many")) 
	                {
	                  ((SelectQuery) newOrQuery)
	                      .addVar(new Var("COUNT(" + lexList.get(i + 2).getSlotEntry(0).getResource() + ")"));
	                }
	              }
	            }
	          }
	        }
	      } // end if whPhraseTree != null
	    } else if (newOrQuery != null) 
	    {
	    	// end if (clauseTag.equals("S")... && newOrQuery != null)
	    	// do nothing
	    }
	    
	    
	    // TTT: chi danh cho What is result of match between Team and Team?        
	    if (nlQuestion.toLowerCase().contains("what")
	        && nlQuestion.toLowerCase().contains("result of")
	        && nlQuestion.toLowerCase().contains("match")
	        && nlQuestion.toLowerCase().contains("between")) 
	    {
	    	// Duyet bien result thay vi tao mot bien moi
	    	Var rs = null;
	    	for(int i=0; i<lexList.size(); i++)
	    	{
	    		if(lexList.get(i).getValue().startsWith("result"))
	    		{
	    			rs = new Var(lexList.get(i).getSlotEntry(0).getResource().getResource());
	    			break;
	    		}
	    	}
	    	
	    	if(rs == null)
	    	{
	    		rs = new Var();
	    	}
	    	
		    Var mtch = new Var(); // Tao bien moi match
		    ((SelectQuery) newOrQuery).addVar(rs);
		    newOrQuery.getWhere().addTriplePattern(new Triple(mtch, new IRI("http://bk.sport.owl#hasResult"), rs));
		    newOrQuery.getWhere().addTriplePattern(new Triple(rs, IRI.RDF_TYPE_IRI, new IRI("http://bk.sport.owl#MatchResult")));
		    newOrQuery.getWhere().addTriplePattern(new Triple(mtch, IRI.RDF_TYPE_IRI, new IRI("http://bk.sport.owl#Match")));
		    newOrQuery.getWhere().addTriplePattern(new Triple(mtch, new IRI("http://bk.sport.owl#firstCompetitor"),andList.get(0).getSlotEntry(0).getResource()));
		    newOrQuery.getWhere().addTriplePattern(new Triple(mtch, new IRI("http://bk.sport.owl#secondCompetitor"),andList.get(1).getSlotEntry(0).getResource()));    
	      
	      System.out.println("TTT return newOrQuery");
	      return newOrQuery;   // not return query  
	    }
	    
	    System.out.println("=======================================================================");
	    System.out.println("newOrQuery.toString()");
	    System.out.println(newOrQuery.toString());
	    System.out.println("=======================================================================");

	    ApplicationFacade.getInstance().sendNotification(ApplicationFacade.LOG_CMD,sos.toString());
	    System.setOut(ps);
	    /*
	    if (newOrQuery.toString().contains("news")) 
	    {
	      System.out.println(newOrQuery.toString());
	    } else {
	    	System.out.println(newOrQuery.toString().replaceAll(
	    	          "(?s)SELECT ?(.*) ?WHERE ?\\{(.*)\\}",
	    	          "SELECT ?news $1 WHERE {\nGRAPH ?news{$2}\n}"));
	    }
	    */
		return query;
	}
}
