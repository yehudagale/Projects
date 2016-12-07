import java.util.HashMap;
import java.util.ArrayList;
/**
* A Class for making HTML DOM trees
*/
public class HTMLTreeNode
{
	private HashMap<String,String> atributes = new HashMap<String, String>();
	private ArrayList<HTMLTreeNode> children = new  ArrayList<HTMLTreeNode>();
	private HTMLTreeNode parent;
	private String tag;
	private String id = null;
	private boolean enteredId;
	/**
	*
	*@param top the parent node
	*@param nodeTag the tag of this node
	*/
	HTMLTreeNode(HTMLTreeNode top, String nodeTag)
	{
		if(top != null){
			top.addChild(this);
		}
		this.tag = nodeTag;
		this.setParent(top);
	}
	// HTMLTreeNode(HTMLTreeNode top, String nodeTag, ArrayList<HTMLTreeNode> newChildren)
	// {
	// 	this(top, nodeTag);
	// 	this.addChildren(newChildren);
	// }
	/**
	* Adds a child to the tree.
	* @param newChild the child to be added.
	*/
	public void addChild(HTMLTreeNode newChild)
	{
		children.add(newChild);
	}
	// public boolean removeChild(HTMLTreeNode child)
	// {
	// 	return children.remove(child);
	// }
	// public void addChildren(ArrayList<HTMLTreeNode> newChildren)
	// {
	// 	children.addAll(newChildren);
	// }
	/**
	*
	* @return an ArrayList of the children
	*/
	public ArrayList<HTMLTreeNode> getChildren()
	{
		return new ArrayList<HTMLTreeNode>(children);
	}
	/**
	*
	* @return This Node's Parent
	*/
	public HTMLTreeNode getParent()
	{
		return parent;
	}
	/**
	* Set's the parent for this node
	* @param top This Node's new Parent
	*/
	public void setParent(HTMLTreeNode top)
	{
		this.parent = top;
	}
	/**
	* Sets the parent for this node
	* @param key The name of the attribute to be returned
	* @return The value of the attribute
	*/
	public String getAtribute(String key)
	{
		return atributes.get(key);
	}
	/**
	* Set's the parent for this node
	* @return A HashMap of all the attributes
	*/
	public HashMap<String, String> getAtributes()
	{
		return new HashMap<String, String>(atributes);
	}
	/**
	* Adds an attribute to this node. If the attribute is id, 
	* records that we have entered it and stores it in the correct feild. 
	* @param key The name of the attribute to be added
	* @param value The value of the attribute to be added
	* @return The value of the attribute that was replaced, if none was replaced, null
	*/
	public String setAtribute(String key, String value)
	{
		if (key.equals("id")){
			String oldId = id;
			this.id = value;
			enteredId = true;
			return oldId;
		}
		return atributes.put(key, value);
	}
	/**
	* Sets the id directly, for use when the user has not entered an ID
	* @param newID the new value of id
	* @return The old value of ID.
	*/
	public String setId(String newId){
		String oldId = id;
		this.id = newId;
		enteredId = false;
		return oldId;
	}
	/**
	* Returns the current ID.
	* @return The value of ID.
	*/
	public String getId()
	{
		return this.id;
	}
	/**
	* Returns a boolean representing whether or not the user entered the ID.
	* @return The value of ID.
	*/
	public boolean enteredId()
	{
		return this.enteredId;
	}
	/**
	* Returns this Node's tag
	* @return This Node's tag
	*/
	public String getTag()
	{
		return this.tag;
	}
}
