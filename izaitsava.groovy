def NAME="izaitsava"

job("MNTLAB-${NAME}-main-build-job"){
  parameters{
    choiceParam('BRANCH_NAME', ["${NAME}", 'main'], 'branch')
    activeChoiceParam('JOB') {
      description('Allows user choose from multiple choices')
      filterable()
      choiceType('CHECKBOX')
        groovyScript {
          script('''
          def list=[]
          [1, 2, 3, 4].each {
            list.add("MNTLAB-${NAME}-child" + it + "-build-job")}
          return list
        ''')
      fallbackScript('"fallback choice"')
    }
   }
  } 
  steps{
    downstreamParameterized {
      trigger("$JOB") {
        block {
          buildStepFailure('FAILURE')
          failure('FAILURE')
          unstable('UNSTABLE')
        }
        parameters {
          predefinedBuildParameters {
            properties("BRANCH_NAME=${BRANCH_NAME}")
            textParamValueOnNewLine(false)
          }
        }                         
      }
    }
  }
}

[1, 2, 3, 4].each {
  job("MNTLAB-${NAME}-child" + it + "-build-job") { 
    scm{
        github('MNT-Lab/dsl-task/', "${BRANCH_NAME}")
    }
    steps{
      shell('chmod +x script.sh && ./script.sh > output.txt' )
      shell("tar czf ${BRANCH_NAME}_dsl_script.tar.gz script.sh")
    }
    publishers{
      archiveArtifacts{
        pattern('output.txt')
        pattern("${BRANCH_NAME}_dsl_script.tar.gz")
        onlyIfSuccessful()
      }
    }
  }
}