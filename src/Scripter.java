/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Scripter {
	public static final int TEST = 10000;
	public static void main(String[] args) throws Throwable {
		final ScriptEngineManager scrMgr = new ScriptEngineManager();
		final ScriptEngine engine = scrMgr.getEngineByName("ECMAScript");
		final Bindings binds = engine.createBindings();
		//
		final String script = "println('hola mundo: ' + i); i++;";
		binds.put("i", "1");
		boolean useCompiled = false;
		if (useCompiled && (engine instanceof Compilable)) {
			final Compilable compiler = (Compilable)engine;
			final CompiledScript compiled = compiler.compile(script);
			long ts = System.currentTimeMillis();
			for (int i = 0; i < TEST; i++)
				compiled.eval(binds);
			System.out.println();
			System.out.println("Compiled time=" + (System.currentTimeMillis() - ts));
		} else {
			long ts = System.currentTimeMillis();
			for (int i = 0; i < TEST; i++)
				engine.eval(script, binds);
			System.out.println();
			System.out.println("Evaled time=" + (System.currentTimeMillis() - ts));
		}
	}
}
