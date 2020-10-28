[1, 2, 3, 4].each {
  job("MNTLAB-izaitsava-child${it}-build-job") {
    parameters {
      activeChoiceParam('BRANCH_NAME') {
        choiceType('SINGLE_SELECT')
          groovyScript {
            script('''def gettags = ("git ls-remote -t -h https://github.com/MNT-Lab/dsl-task.git ").execute()
                      return gettags.text.readLines().collect { 
                       it.split()[1].replaceAll('refs/heads/', '').replaceAll('refs/tags/', '').replaceAll("\\\\^\\\\{\\\\}", '')}
                   ''')
                }
            }
        }
    scm{
        github('MNT-Lab/dsl-task', '${BRANCH_NAME}')
    }
    steps{
      shell('chmod +x script.sh && ./script.sh > output.txt' )
      shell('tar czf ${BRANCH_NAME}_dsl_script.tar.gz script.sh')
    }
    publishers{
      archiveArtifacts{
        pattern('output.txt')
        pattern('${BRANCH_NAME}_dsl_script.tar.gz')
        onlyIfSuccessful()
      }
    }
  }
} 


job("MNTLAB-izaitsava-main-build-job"){
  parameters{
    choiceParam('BRANCH_NAME', ['izaitsava', 'main'])
    activeChoiceParam('JOBA') {
      description('Allows user choose from multiple choices')
      choiceType('CHECKBOX')
        groovyScript {
          script('''
          def list=[]
          [1, 2, 3, 4].each {
            list.add("MNTLAB-izaitsava-child${it}-build-job")}
          return list
        ''')
    }
   }
  } 
  steps{
    downstreamParameterized {
      trigger('$JOBA') {
        block {
          buildStepFailure('FAILURE')
          failure('FAILURE')
          unstable('UNSTABLE')
        }
        parameters {
          predefinedBuildParameters {
            properties('BRANCH_NAME=${BRANCH_NAME}')
            textParamValueOnNewLine(false)
          }
        }                         
      }
    }
  }
}
