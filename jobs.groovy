us = 'vsakhonchik'


    job("MNTLAB-$us-main-build-job") {
        scm {
            github('MNT-Lab/dsl-task', "$us")
        }
        parameters {
            choiceParam('BRANCH_NAME', ["$us", 'vsakhonchik'])
            activeChoiceParam('JOB') {
                description('Select child jobs')
                choiceType('CHECKBOX')
                groovyScript {
                    script('us = "vsakhonchik"; List<String> list = new ArrayList<String>(); for (i = 1; i <5; i++) { list.add("MNTLAB-$us-child$i-build-job") } ; return list')
                }
            }
        }
        steps {
            downstreamParameterized {
                trigger('$JOB') {
                    block {
                        buildStepFailure('FAILURE')
                        failure('FAILURE')
                        unstable('UNSTABLE')
                    }
                    parameters {
                        predefinedProp('BRANCH_NAME', '$BRANCH_NAME')
                }                 
               
            }
            
        }

}   
   for(i in 1..4) {
    job("MNTLAB-$us-child${i}-build-job") {
        scm {
            github('MNT-Lab/dsl-task', "$us")
        }
        parameters {
           activeChoiceParam('Branch') {
          groovyScript {
              script('''
                     def gitURL = "https://github.com/MNT-Lab/dsl-task.git"
    def command = "git ls-remote -h $gitURL"
    def proc = command.execute()
    proc.waitFor()              
    
    def branches = proc.in.text.readLines().collect { 
        it.replaceAll(/[a-z0-9]*\\trefs\\/heads\\//, '') 
    }
    return branches ''')
               }  
            
               
       steps {
            shell('chmod +x script.sh')
            shell('./script.sh > output.txt')
            shell("tar -cvf ${us}_dsl_script.tar.gz output.txt jobs.groovy script.sh") 
        }
       publishers { 
			archiveArtifacts('output.txt')
			archiveArtifacts('jobs.groovy')
	       archiveArtifacts("${us}_dsl_script.tar.gz")
			
}
   }
} 
   }    
   } 
    }