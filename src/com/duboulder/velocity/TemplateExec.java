package com.duboulder.velocity;

import java.io.*;
import org.apache.velocity.context.*;

/**
 * A velocity template execution service that uses a TemplateLoader.
 */
public interface TemplateExec {
	/**
	 * The name of the execution service
	 * @return the service name (not null, not empty)
	 */
	String getName ();

	/**
	 * The template path prefix
	 * @return the prefix (not null, may be empty)
	 */
	String getPrefix ();
	/**
	 * Set the path prefix
	 * @param prefix the new prefix (may be null)
	 */
	void setPrefix (String prefix);

	/**
	 * The path suffix. The default depends on the implementation but
	 * will usually be .vm.
	 * @return the suffix (not null, may be empty)
	 */
	String getSuffix ();
	/**
	 * Set the path suffix
	 * @param suffix the new suffix (may be null)
	 */
	void setSuffix (String suffix);

	/**
	 * Execute the template and append the results to the output.
	 * @param templatePath the path for the template to execute (not null, not empty)
	 * @param context the execution context with the template values (not null)
	 * @param output the output to append to (not null)
	 * @throws VelocityExecExption on template execution errors
	 */
	void execute (String templatePath, Context context, PrintWriter output);
}
