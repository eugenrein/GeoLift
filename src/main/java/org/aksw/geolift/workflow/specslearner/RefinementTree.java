/**
 * 
 */
package org.aksw.geolift.workflow.specslearner;

import java.util.List;
import java.util.Set;

/**
 * @author sherif
 *
 */
public class RefinementTree extends Tree<RefinementNode> {
	public static final double CHILDREN_MULTIPLIER = 1;
	
	private void setFitness(Tree<RefinementNode> root, double fitness){
		long rootChildrenCount = root.size() - 1;
		root.getValue().fitness += fitness + CHILDREN_MULTIPLIER * rootChildrenCount;
		root = root.getParent();
		while(root != null){
			root.getValue().fitness += CHILDREN_MULTIPLIER * rootChildrenCount;
			root = root.getParent();
		}
		
	}

	/**
	 * 
	 *@author sherif
	 */
	public RefinementTree() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 *@author sherif
	 */
	public RefinementTree(RefinementNode value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param parent
	 * @param value
	 * @param childrenlist
	 *@author sherif
	 */
	public RefinementTree(Tree<RefinementNode> parent, RefinementNode value,
			List<Tree<RefinementNode>> childrenlist) {
		super(parent, value, childrenlist);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#getLeaves()
	 */
	@Override
	public Set<Tree<RefinementNode>> getLeaves() {
		// TODO Auto-generated method stub
		return super.getLeaves();
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#addChild(org.aksw.geolift.workflow.specslearner.Tree)
	 */
	@Override
	public void addChild(Tree<RefinementNode> child) {
		// TODO Auto-generated method stub
		super.addChild(child);
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#removeChild(org.aksw.geolift.workflow.specslearner.Tree)
	 */
	@Override
	public void removeChild(Tree<RefinementNode> child) {
		// TODO Auto-generated method stub
		super.removeChild(child);
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#getParent()
	 */
	@Override
	public Tree<RefinementNode> getParent() {
		// TODO Auto-generated method stub
		return super.getParent();
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#getchildren()
	 */
	@Override
	public List<Tree<RefinementNode>> getchildren() {
		// TODO Auto-generated method stub
		return super.getchildren();
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#getValue()
	 */
	@Override
	public RefinementNode getValue() {
		// TODO Auto-generated method stub
		return super.getValue();
	}


//	/* (non-Javadoc)
//	 * @see org.aksw.geolift.workflow.specslearner.Tree#print(org.aksw.geolift.workflow.specslearner.Tree)
//	 */
//	@Override
//	public void print(Tree<RefinementNode> root) {
//		// TODO Auto-generated method stub
//		super.print(root);
//	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#size()
	 */
	@Override
	public long size() {
		// TODO Auto-generated method stub
		return super.size();
	}
	
}
