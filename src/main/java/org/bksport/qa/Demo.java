package org.bksport.qa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.util.List;

//import edu.mit.jwi.Dictionary;
//import edu.mit.jwi.IDictionary;
//import edu.mit.jwi.item.IIndexWord;
//import edu.mit.jwi.item.ISenseKey;
//import edu.mit.jwi.item.ISynset;
//import edu.mit.jwi.item.IWord;
//import edu.mit.jwi.item.IWordID;
//import edu.mit.jwi.item.LexFile;
//import edu.mit.jwi.item.POS;
//import edu.mit.jwi.item.SenseKey;
//import edu.mit.jwi.item.Synset;
import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import eu.monnetproject.lemon.LemonFactory;
import eu.monnetproject.lemon.LemonModel;
import eu.monnetproject.lemon.LemonModels;
import eu.monnetproject.lemon.LemonSerializer;
import eu.monnetproject.lemon.LinguisticOntology;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.Lexicon;
import eu.monnetproject.lemon.model.Text;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;
import net.lexinfo.LexInfo;

import org.bksport.qa.nlq.NLQParser;
import org.bksport.qa.nlq.NLQParser2;
import org.bksport.qa.nlq.ResourceRecognizer;
import org.bksport.qa.util.ConfigUtil;
import org.bksport.qa.util.FileUtil;
import org.bksport.qa.util.NSUtil;
import org.bksport.sparql.BlankNode;
import org.bksport.sparql.Constraint;
import org.bksport.sparql.Expression;
import org.bksport.sparql.Function;
import org.bksport.sparql.GroupGraphPattern;
import org.bksport.sparql.IRI;
import org.bksport.sparql.Node;
import org.bksport.sparql.OrderCondiction;
import org.bksport.sparql.Query;
import org.bksport.sparql.RDFLiteral;
import org.bksport.sparql.SelectQuery;
import org.bksport.sparql.SolutionModifier;
import org.bksport.sparql.Triple;
import org.bksport.sparql.Var;
import org.bksport.sparql.expression.EqualExpression;
import org.bksport.sparql.function.LangFunction;

/**
 * 
 * Demo for all related APIs
 * 
 * @author congnh
 * 
 */
public class Demo {

  static MaxentTagger      tagger;
  static LexicalizedParser lp;
  static {
    ConfigUtil.reload();
    tagger = new MaxentTagger(FileUtil.getAbsolutePath("model",
        "standford-pos-tagger", "english-left3words-distsim.tagger"));
    lp = LexicalizedParser.loadModel(FileUtil.getAbsolutePath("model",
        "standford-parser", "englishPCFG.ser.gz"));
    ResourceRecognizer.loadResourceDataset();
  }

  public static void demoLemon() {
    final LemonSerializer serializer = LemonSerializer.newInstance();
    try {
      serializer.read(new FileReader(new File(FileUtil.getAbsolutePath("data",
          "uby-example.rdf"))));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    final LemonModel model = serializer.create();
    final Lexicon lexicon = model.addLexicon(
        URI.create("http://www.example.com/mylexicon"), "en" /* English */);
    final LexicalEntry entry = LemonModels.addEntryToLexicon(lexicon,
        URI.create("http://www.example.com/mylexicon/cat"), "cat",
        URI.create("http://dbpedia.org/resource/Cat"));

    final LemonFactory factory = model.getFactory();
    final LexicalForm pluralForm = factory.makeForm(URI
        .create("http://www.example.com/mylexicon/cats"));
    pluralForm.setWrittenRep(new Text("cats", "en"));
    final LinguisticOntology lingOnto = new LexInfo();
    pluralForm.addProperty(lingOnto.getProperty("number"),
        lingOnto.getPropertyValue("plural"));
    entry.addOtherForm(pluralForm);
    System.out.println("Serialization");
    serializer.writeEntry(model, entry, lingOnto, new OutputStreamWriter(
        System.out));
    serializer.close();

  }

  public static void demoStandfordPosTagger(String text) {
    String taggedText = tagger.tagString(text);
    System.out.println("origin: " + text);
    System.out.println("tagged: " + taggedText);
  }

  public static void demoStandfordParser(String text) {
    // String[] words = { "Did", "Barcelona", "win", "Arsenal", "?" };
    // List<CoreLabel> rawWords = Sentence.toCoreLabelList(words);
    // Tree parse = lp.apply(rawWords);
    // parse.pennPrint();

    // This option shows loading and using an explicit tokenizer
    System.out.println("origin: " + text);
    TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
        new CoreLabelTokenFactory(), "");
    List<CoreLabel> rawWords2 = tokenizerFactory.getTokenizer(
        new StringReader(text)).tokenize();
    Tree parse = lp.apply(rawWords2);
    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
    System.out.println(tdl);

    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
    tp.printTree(parse);
  }

  public static void demoSparql() {
    Query query = new SelectQuery();
    query.addPrefix("bksport", new IRI(NSUtil.bksport));
    GroupGraphPattern ggp = new GroupGraphPattern();
    GroupGraphPattern ggp2 = new GroupGraphPattern();
    SolutionModifier sm = new SolutionModifier();

    Node s1 = new IRI(query, "bksport", "lionel-messi");
    Node p1 = new IRI(query, "bksport", "playFor");
    Node o1 = new IRI(query, "bksport", "barcelona-fc");
    Triple tp1 = new Triple(s1, p1, o1);

    Node s2 = new IRI(NSUtil.bksport("chelsea-fc"));
    Node p2 = new BlankNode();
    Node o2 = new Var();
    Triple tp2 = new Triple(s2, p2, o2);

    Function f1 = new LangFunction((Var) o2);
    RDFLiteral str = new RDFLiteral("en");
    Expression equals = new EqualExpression(f1, str);
    f1.setExpression((Var) o2);
    Constraint c1 = new Constraint(equals);

    OrderCondiction oc1 = new OrderCondiction(f1);

    ggp2.addTriplePattern(tp1);
    ggp2.addTriplePattern(tp2);
    ggp2.addFilter(c1);

    ggp.addTriplePattern(tp1);
    ggp.addTriplePattern(tp2);
    ggp.addFilter(c1);
    ggp.addOptional(ggp2);

    sm.addCondiction(oc1);
    ((SelectQuery) query).addVar((Var) o2);
    ((SelectQuery) query).setSolution(sm);
    query.setWhere(ggp);
    System.out.println(query.toString());
  }

  public static void demoWordNet() {
    // IDictionary dict = new Dictionary(new
    // File("C:/Program Files (x86)/WordNet/2.1/dict"));
    // try {
    // dict.open();
    // ISenseKey senseKey = new SenseKey("said", 1, POS.VERB, false,
    // LexFile.VERB_BODY);
    // IWord wo = dict.getWord(senseKey);
    // System.out.println(wo.getSynset().getWords().get(0));
    // IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
    // IWordID wordId = idxWord.getWordIDs().get(0);
    // IWord word = dict.getWord(wordId);
    // System.out.println("ID = " + wordId);
    // System.out.println("Lema = " + word.getLemma());
    // System.out.println("Gloss = " + word.getSynset().getGloss());
    // ISynset synset = word.getSynset();
    // for(IWord w: synset.getWords()){
    // System.out.println(w);
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    try {
      JWNL.initialize(new FileInputStream(FileUtil.getAbsolutePath("conf",
          "jwnl", "file_properties.xml")));
      // publish
      IndexWord indexWord = Dictionary.getInstance().getIndexWord(POS.NOUN,
          "title");
      Synset[] set = indexWord.getSenses();
      // Pointer[] pointerArr = set[0].getPointers(PointerType.REGION_MEMBER);
      // for (Pointer x : pointerArr) {
      // Synset target = x.getTargetSynset();
      // for(int i=0; i< target.getWordsSize(); i++){
      // System.out.println(target.getWord(i).getLemma());
      // }
      // }
      for (int i = 0; i < set.length; i++) {
        for (int j = 0; j < set[i].getWordsSize(); j++) {
          System.out.println(set[i].getWord(j).getLemma());
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (JWNLException e) {
      e.printStackTrace();
    }
  }

  public static void demoMorphology() {
    Morphology mor = new Morphology();
    System.out.println(mor.stem("championships"));
    System.out.println(mor.stem("wrote"));
    System.out.println(mor.stem("won"));
    System.out.println(mor.stem("saw"));
    System.out.println(mor.stem("scored"));
    System.out.println(mor.stem("oversaw"));
    System.out.println(mor.stem("published"));
    System.out.println(mor.stem("took"));
    System.out.println(mor.stem("taken"));
  }

  public static void main(String args[]) {
    List<String> questionList = FileUtil.readFileAsList(FileUtil
        .getAbsolutePath("data", "bksport-question-dataset.txt"));
    for (int i = 0; i < questionList.size(); i++) {
      if (!questionList.get(i).isEmpty()
          && !questionList.get(i).startsWith("#")) {
        // Demo.demoStandfordParser(questionList.get(i));
        System.out.println(NLQParser2.parse2(questionList.get(i)));
      }
    }
    // Demo.demoStandfordPosTagger("Who score the last goal and win Vandersa and defeat Barca?");
    // Demo.demoStandfordPosTagger("which team won Premier League 2013's championship?");
    // Demo.demoStandfordPosTagger("is Premier League 2013's championship won by MU?");
    // Demo.demoStandfordPosTagger("by whom is Premier League 2013's championship won?");
    // Demo.demoStandfordPosTagger("Where does Premier League 2013 take place in?");
    // Demo.demoSparql();
    // Demo.demoLemon();
    // Demo.demoWordNet();
    // Demo.demoMorphology();
  }
}
