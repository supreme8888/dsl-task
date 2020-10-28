myname = "mshnipau"

    job("MNTLAB-$myname-main-build-job") {
        scm {
            github('MNT-Lab/dsl-task', "$myname")
        }
      
        parameters {
            choiceParam('BRANCH_NAME', ["$myname", 'main'])
            activeChoiceParam('JOB') {
                description('Select child jobs')
                choiceType('CHECKBOX')
                groovyScript {
                    script('''
myname = "mshnipau"
list = []
for (i in 1..4) {list.add("MNTLAB-$myname-child$i-build-job")}
return list
                    ''')
                }
            }
        }
      
        steps {
            downstreamParameterized {
              trigger( "\$JOB" ) {
                block {
                  buildStepFailure('FAILURE')
                  failure('FAILURE')
                  unstable('UNSTABLE')
                }
                
               parameters {
                 predefinedProp('BRANCH_NAME', "\$BRANCH_NAME" )
               }                 
              }   
            }
       }

      
   for(i in 1..4) {
     job("MNTLAB-$myname-child$i-build-job") {
        parameters {
          activeChoiceParam('BRANCH_NAME') {
            description('branchs with name of students')
            choiceType('SINGLE_SELECT')
            groovyScript {
              script('["$BRANCH_NAME"]')
              fallbackScript('''
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
def urlText(adress) {
  def jsonSlurper = new JsonSlurper() 
  parse=(new URL("$adress").text.trim())
  new_parse=[]
  def object = jsonSlurper.parseText(parse)
  object.each {new_parse.add(it.name)}
  return(new_parse.sort())
}
urlText("https://api.github.com/repos/MNT-Lab/dsl-task/branches")
              ''')
            }
          }
        }

       scm {
          github('MNT-Lab/dsl-task', '$BRANCH_NAME')
       }
               
       steps {
         shell("chmod +x script.sh \n ./script.sh > output.txt \n tar cvfz \${BRANCH_NAME}_dsl_script.tar.gz output.txt script.sh") 
       }
             
       publishers { 
         archiveArtifacts(" \${BRANCH_NAME}_dsl_script.tar.gz , output.txt")  
         wsCleanup()
       }
    }
  } 
}
