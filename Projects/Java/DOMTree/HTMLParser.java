/**
* used http://software.hixie.ch/utilities/js/live-dom-viewer/ for visualisation and
* https://www.w3.org/TR/html-markup/syntax.html#syntax-attr-single-quoted for definitions.
* used Some of Professor Diament's code for the Depth first and Breath first search functions
* A class for parsing a basic HTML string into a DOM tree
*
*/
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
public class HTMLParser{
	private static final  String[] SPACE = new String[]{" "};
	private static final Pattern CHECKING_PATTERN = Pattern.compile("[^ =]+ *=.*");
	private static final Pattern REPLACING_PATTERN = Pattern.compile("[^ =]* *= *");
	private HTMLTreeNode rootNode;
	private HashMap<String, HTMLTreeNode> idMap;
	private int id;
	public static void main(String[] args) {
		HTMLParser thing = new HTMLParser();
		thing.parse("<p id = \"2\">test<b id=1 blah pie  = 'more pie'>more test</b> even more test</p>");
		thing.depthFirst();
		thing.breadthFirst();
		thing.parse("<p>don't be here</p>");
		if(args.length != 0){
			System.out.println("---YOUR EXAMPLE---");
			thing.parse(args[0]);
			thing.depthFirst();
			thing.breadthFirst();
		}
	}
	HTMLParser()
	{
	}
	/**
	* Resets the neccesary feilds, makes a dummy root node, and calls the main parse method on it.
	* @param docString The String to be parsed
	*/
	public void parse(String docString)
	{
		this.id = 0;
		this.idMap = new HashMap<String, HTMLTreeNode>();
		rootNode = new HTMLTreeNode(null, "DOMTree");
		parse(docString, rootNode);
	}
	/**
	* Parses the string recursivly, creating new nodes, moving up the tree
	* or making new text nodes as neccesary. 
	* @param docString String to be parsed
	* @param StartNode Node to start parsing from
	*/
	private void parse(String docString, HTMLTreeNode startNode)
	{
		if(docString.isEmpty()){
			return;
		}
		else if(docString.startsWith("</")){
			parse(docString.substring(docString.indexOf(">") + 1), startNode.getParent());
		}
		else if(docString.startsWith("<")){
			parse(docString.substring(docString.indexOf(">") + 1),
					newNode(startNode, docString.substring(1, docString.indexOf(">"))));
		}
		else{
			newTextNode(startNode, docString.substring(0, docString.indexOf("<")));
			parse(docString.substring(docString.indexOf("<")), startNode);
		}
	}
	/**
	* Creates a new Text Node by creating the correct String and calling newNode
	* @param parent The parent of the new node
	* @param NodeString The text in the new node
	* @return The new Node
	*/
	private HTMLTreeNode newTextNode(HTMLTreeNode parent, String nodeString)
	{
		return newNode(parent, "text content=\"" + nodeString + "\"");
	}
	/**
	* Creates a new Node
	* @param parent The parent of the new node
	* @param NodeString A string representing the tag and attributes of the new node
	* @return The new Node
	*/
	private HTMLTreeNode newNode(HTMLTreeNode parent, String nodeString)
	{
		int endIndex = indexingMin(nodeString, SPACE);
		HTMLTreeNode thisNode = new HTMLTreeNode(parent, nodeString.substring(0, endIndex));
		addAtributes(nodeString.substring(endIndex), thisNode);
		assignId(thisNode);
		return thisNode;
	}
	/**
	* Recursivly parses the attributes string
	* @param attributes A string representing the attributes of the node
	* @param node the node that has the attributes
	*/
	private static void addAtributes(String attributes, HTMLTreeNode node)
	{
		attributes = attributes.trim();
		if(attributes.isEmpty()){
			return;
		}
		int endIndex = 0;
		// adjuster allows me to use only one recursive call.
		byte adjuster = 0;
		if (CHECKING_PATTERN.matcher(attributes).matches()) {
			String attributeName = attributes.substring(0, indexingMin(attributes, new String[]{" ", "="}));
			attributes = REPLACING_PATTERN.matcher(attributes).replaceFirst("");
			char qoute = attributes.charAt(0); 
			if (qoute == '\'' || qoute == '"'){
				attributes = attributes.substring(1);
				endIndex = attributes.indexOf(qoute);
				adjuster++;
			}
			else{
				endIndex = indexingMin(attributes,  SPACE);
			}
			node.setAtribute(attributeName, attributes.substring(0, endIndex));
		}
		else{
			endIndex = indexingMin(attributes, SPACE);
			node.setAtribute(attributes.substring(0, endIndex), "");
		}
		addAtributes(attributes.substring(endIndex + adjuster), node);
	}
	/**
	* assigns a unique ID to a node
	* @param node the node to be assinged an ID
	*/
	private void assignId(HTMLTreeNode node)
	{
		String nodeId = node.getId();
		if (nodeId != null){
			if (idMap.containsKey(nodeId)){
				HTMLTreeNode otherNode = idMap.get(nodeId);
				if (otherNode.enteredId()) {
					System.out.println("duplicate id entered");
				}
				//put this in an else block to allow duplicate ids
				otherNode.setId(null);
				this.assignId(otherNode);
				//end else block
			}
			idMap.put(nodeId, node);
		}
		else{
			for(nodeId = "" + id; idMap.putIfAbsent(nodeId,node) != null ; id++){
				nodeId = "" + id;
			}
			node.setId(nodeId);
		}
	}
	/**
	* Returns the index of the first string in testingStrings that apears in testString. If none of them apear,
	* returns the length of the testString.
	* @param testString the string to be tested
	* @param testingStrings the strings to test
	* @return The index of the first string in testingStrings that apears in testString
	*/
	private static int indexingMin(String testString, String[] testingStrings)
	{
		int min = testString.length();
		int indexer = 0;
		for (String test : testingStrings){
			indexer = testString.indexOf(test);
			if(indexer != -1 && indexer < min){
				min = indexer;
			}
		}
		return min;
	}
	/**
	* Preforms a breadth first search on the Tree that has been parsed
	*/
	public void breadthFirst()
	{
		breadthFirst(rootNode.getChildren().get(0));
	}
	/**
	* Preforms a depth first search on the Tree that has been parsed
	*/
	public void depthFirst()
	{
		depthFirst(rootNode.getChildren().get(0));
	}
	/**
	* Preforms a breadth first search on the node, printing information about it as it goes
	* @param node the node to start from
	*/
	public static void breadthFirst(HTMLTreeNode node)
	{
		LinkedBlockingQueue<HTMLTreeNode> queue = new LinkedBlockingQueue<>();
		queue.add(node);
		while(!queue.isEmpty()){
			HTMLTreeNode current = queue.remove();
			visitNode(current);
			for(HTMLTreeNode child : current.getChildren()){
				queue.add(child);
			}
		}
	}
	/**
	* Preforms a depth first search on the node, printing information about it as it goes
	* @param node the node to start from
	*/
	private static void depthFirst(HTMLTreeNode tree)
	{
		visitNode(tree);
		for(HTMLTreeNode node : tree.getChildren())
		{
			depthFirst(node);
		}
	}
	/**
	* Prints information about a node
	* @param node the node to print information about
	*/
	private static void visitNode(HTMLTreeNode node)
	{
		System.out.println("\nid: " + node.getId());
		System.out.println("tag: " + node.getTag());
		HashMap<String, String> attributes = node.getAtributes();
		for(String attribute : attributes.keySet()){
			System.out.println("attribute: " + attribute + " value: " + attributes.get(attribute));
		}
	}
}