myname="sshevtsov"

(1..4).each{
  job_name="MNTLAB-$myname-child$it-build-job"
  job("$job_name") {
  description 'Child$it Build joba'
  parameters {
        activeChoiceParam("BRANCH_NAME") {
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
        git {
          remote {
            github('MNT-Lab/dsl-task')
          }
          branch('''*/$BRANCH_NAME''')
        }

      steps {
        shell ('tar -cf ${BRANCH_NAME}_dsl_script.tar.gz *')
        shell ("chmod +x script.sh")
       	line="./script.sh > output.txt"
        shell (line)
    }

      publishers {
        archiveArtifacts {
          pattern('${BRANCH_NAME}_dsl_script.tar.gz')
            pattern('output.txt')
            onlyIfSuccessful()
        }
        wsCleanup()
      }
	}
	}
 }


job("MNTLAB-$myname-main-build-job-TEST"){
     parameters {
        choiceParam('BRANCH_NAME',["$myname", 'main'])
        activeChoiceParam("BUILDS_TRIGGER") {
            choiceType('CHECKBOX')
            groovyScript {
              script('''
l=[];
(1..4).each { l.add("MNTLAB-sshevtsov-child$it-build-job") };
return(l);
''')
            }
        }

	scm {
        git {
          remote {
            github('MNT-Lab/dsl-task')
          }
          branch("*/$myname")
        }
	}
    }

  steps {
        downstreamParameterized {
            trigger('$BUILDS_TRIGGER') {
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
}
