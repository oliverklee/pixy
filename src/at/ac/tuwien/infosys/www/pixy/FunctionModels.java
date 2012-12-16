package at.ac.tuwien.infosys.www.pixy;

import java.util.*;

// container class for builtin function models
public class FunctionModels {

    private Set<String> f_evil;
    private Map<String,Set<Integer>> f_multi;
    private Map<String,Set<Integer>> f_invMulti;
    private Set<String> f_strongSanit;
    private Map<String,Set<Integer>> f_weakSanit;
    
    private Set<String> allModels;

    public FunctionModels(Set<String> f_evil, Map<String,Set<Integer>> f_multi,
            Map<String,Set<Integer>> f_invMulti, Set<String> f_strongSanit,
            Map<String,Set<Integer>> f_weakSanit) {
        
        this.f_evil = f_evil;
        this.f_multi = f_multi;
        this.f_invMulti = f_invMulti;
        this.f_strongSanit = f_strongSanit;
        this.f_weakSanit = f_weakSanit;
        
        this.allModels = new HashSet<String>();
        this.allModels.addAll(f_evil);
        this.allModels.addAll(f_multi.keySet());
        this.allModels.addAll(f_invMulti.keySet());
        this.allModels.addAll(f_strongSanit);
        this.allModels.addAll(f_weakSanit.keySet());
        
    }

    public Set<String> getF_evil() {
        return f_evil;
    }

    public Map<String, Set<Integer>> getF_invMulti() {
        return f_invMulti;
    }

    public Map<String, Set<Integer>> getF_multi() {
        return f_multi;
    }

    public Set<String> getF_strongSanit() {
        return f_strongSanit;
    }

    public Map<String, Set<Integer>> getF_weakSanit() {
        return f_weakSanit;
    }
    
    public boolean isModelled(String funcName) {
        return this.allModels.contains(funcName);
    }

}
