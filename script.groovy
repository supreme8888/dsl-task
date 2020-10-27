[1, 2, 3, 4].each {
  job("MNTLAB-shryshchanka-child" + it + "-build-job") {
    parameters {
      gitParameter{
        name('BRANCH_NAME')
        description('')
        branch('')
        useRepository('')
        quickFilterEnabled(false)
        sortMode('NONE')
        type('PT_BRANCH')
        tagFilter('*')
        branchFilter('origin/(.*)')
        defaultValue('origin/main')
        selectedValue('DEFAULT')
        listSize("1")
        }
   
    }  
    scm {
        git {
          remote {
            github('MNT-Lab/dsl-task', 'ssh')
            credentials('32894695-c296-4b7d-a9d1-d66d35a9b476')
          }
          branch('*/${BRANCH_NAME}')
        }
      }
    steps {
      shell('chmod +x script.sh && ./script.sh > output.txt' )
      shell('#!/bin/bash' + '\n' + 'if [ -f *.groovy ]; then' + '\n' + '  tar cvzf ${BRANCH_NAME}_dsl_script.tar.gz *.groovy script.sh' + '\n' + 'else' + '\n' + '  tar cvzf ${BRANCH_NAME}_dsl_script.tar.gz script.sh' + '\n' + 'fi')
    }
    
    publishers {
      archiveArtifacts {
        pattern('output.txt, ${BRANCH_NAME}_dsl_script.tar.gz')
      }
    }
  }
}

job('MNTLAB-shryshchanka-main-job'){
  parameters{
    choiceParam('BRANCH_NAME', ['shryshchanka (default)', 'main'], 'branch')
      activeChoiceParam('BUILDS_TRIGGER') {
          description('')
          filterable(false)
          choiceType('CHECKBOX')
          groovyScript {
            script('return ["MNTLAB-shryshchanka-child1-build-job", "MNTLAB-shryshchanka-child2-build-job", "MNTLAB-shryshchanka-child3-build-job", "MNTLAB-shryshchanka-child4-build-job"]')
            fallbackScript('"fallback choice"')
          }
      }       
  }
  steps{
    downstreamParameterized {
      trigger('${BUILDS_TRIGGER}') {
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