myname = 'vgulinskiy'

    job("MNTLAB-$myname-main-build-job") {
        scm {
            github('MNT-Lab/dsl-task', "$myname")
        }
        parameters {
            choiceParam('BRANCH_NAME', ["$myname", 'main'])
            activeChoiceParam('JOB_NAME') {
                description('Select child jobs')
                choiceType('CHECKBOX')
                groovyScript {
                    script('''
myname = "vgulinskiy"
List<String> list = new ArrayList<String>(); 
for (i = 1; i <5; i++) 
 list << ("MNTLAB-$myname-child$i-build-job") ; 
  return list
''')
                }
            }
        }
        steps {
            downstreamParameterized {
              trigger( "\$JOB_NAME" ) {
                block {
                  buildStepFailure('FAILURE')
                  failure('FAILURE')
                  unstable('UNSTABLE')
                }
                
               parameters {
                 predefinedProp('BRANCH_NAME', '\$BRANCH_NAME' )
               }                 
               
             }   
            }

       }

      
   for(i in 1..4) {
    job("MNTLAB-$myname-child$i-build-job") {
        parameters {
          activeChoiceParam('BRANCH_NAME') {
            description('branch name')
            choiceType('SINGLE_SELECT')
            groovyScript {
              script('["$BRANCH_NAME"]')
              fallbackScript('''
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def urlText(adres)
{
  def jsonSlurper = new JsonSlurper()
   
l=(new URL("$adres").text.trim())
 new_l=[]
 
def object = jsonSlurper.parseText(l)
println(object)
  
  object.each {new_l.add(it.name)}
  return(new_l.sort())
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
         shell(" echo \$BRANCH_NAME \n chmod +x script.sh \n ./script.sh > output.txt \n tar cvfz \${BRANCH_NAME}_dsl_script.tar.gz output.txt script.sh") 
        }
             
       publishers { 
         archiveArtifacts(" \${BRANCH_NAME}_dsl_script.tar.gz , output.txt")
        }
    }
  } 
}
