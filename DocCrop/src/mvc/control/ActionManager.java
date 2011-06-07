package mvc.control;

import java.awt.event.ActionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

/**
 * <p>
 * This class manages actions for an owner object class. Methods in the owner
 * class annotated by @Action are identified as an Action methods, and the
 * functionality of these methods should be identical to the functionality of
 * {@link java.awt.event.ActionListener#actionPerformed(ActionEvent)}.
 * </p>
 * 
 * <p>
 * The name of an Action method is used when calling getAction() to construct an
 * AbstractAction which uses the method's defined action. All properties related
 * to an AbstractAction are derived from the associated ResourceBundle, which
 * has to contain specific entries for the actions that are requested.
 * </p>
 * 
 * <p>
 * An entry in the ResourceBundle file should follow this template: <br>
 * <i>full classname</i>.<i>method name</i>.<i>property</i> where property
 * refers to one of the following:
 * 
 * <li>text - text to display as name for the associated component</li>
 * <li>mnemonic - single character that can be used to quickly invoke associated
 * component</li>
 * <li>accelerator - shortcut key to use</li>
 * <li>shortdesc - hovering popup description, aka tooltip text</li>
 * </p>
 * 
 * 
 * @author Håkan Waag, with adjustments made by Tomas Toss
 * 
 */
public class ActionManager {

	private HashMap<String, AbstractAction> actionMap = new HashMap<String, AbstractAction>();

	/**
	 * 
	 * @param owner
	 *            owner class of this ActionManager, very likely equal to
	 *            <i>this.getClass()</i>
	 */
	@SuppressWarnings("serial")
	public ActionManager(final Object owner, ResourceBundle resource) {

		Class<?> ownerClass = owner.getClass();

		/*
		 * iterate over all methods available in the specified owner class
		 */
		Method[] methods = ownerClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			final Method method = methods[i];
			/*
			 * See if this particular method is annotated by @Action. The new
			 * action is then constructed using data from the specified
			 * ResourceBundle.
			 */
			if (method.isAnnotationPresent(Action.class)) {
				String actionName = method.getName();

				AbstractAction newAction = new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							method.invoke(owner);
						} catch (IllegalArgumentException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IllegalAccessException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (InvocationTargetException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				};
				String prefix = ownerClass.getName() + "." + actionName + ".";
				// always load name from resources, this forces the existence of
				// this entry
				newAction.putValue(AbstractAction.NAME,
						resource.getString(prefix + "text"));
				// load mnemonic for this action if it exists in the resources
				if (resource.containsKey(prefix + "mnemonic")) {
					String stringMnemonic = resource.getString(prefix
							+ "mnemonic");
					int integerMnemonic = new Integer(stringMnemonic.charAt(0));
					newAction.putValue(AbstractAction.MNEMONIC_KEY,
							integerMnemonic);
				}
				// load accelerator for this action if it exists in the
				// resources
				if (resource.containsKey(prefix + "accelerator")) {
					newAction.putValue(
							AbstractAction.ACCELERATOR_KEY,
							KeyStroke.getKeyStroke(resource.getString(prefix
									+ "accelerator")));
				}
				// load short description (aka tooltip) from resources, if it
				// exists
				if (resource.containsKey(prefix + "shortdesc")) {
					newAction.putValue(AbstractAction.SHORT_DESCRIPTION,
							resource.getString(prefix + "shortdesc"));
				}

				actionMap.put(actionName, newAction);
			}
		}
	}

	/**
	 * This method constructs an AbstractAction based on specified name. The
	 * properties of the AbstractAction is determined by the ResourceBundle
	 * supplied with the ActionManager. The behavior of the AbstractAction is
	 * determined by the method implemented in the owner class with the same
	 * name (and annotated with @Action) as the inputed string.
	 * 
	 * @param actionName
	 *            name of the action, refers to method name and its name in the
	 *            ResourceBundle
	 * @return an AbstractAction using available resources based on it's name
	 * @throws Exception
	 */
	public AbstractAction getAction(String actionName) {
		if (actionMap.containsKey(actionName))
			return actionMap.get(actionName);
		else
			return null;
	}
}
