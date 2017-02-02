package au.org.aekos.api.model;

import java.util.Collection;
import java.util.List;

class Helper {
	
	static boolean matchesFilter(List<String> traitOrVarNames, Collection<TraitOrEnvironmentalVariable> traitsOrVars) {
		if (traitOrVarNames.size() == 0) {
			return true;
		}
		for (TraitOrEnvironmentalVariable curr : traitsOrVars) {
			if (traitOrVarNames.contains(curr.getName())) {
				return true;
			}
		}
		return false;
	}
}
