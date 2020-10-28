def NAME='izaitsava'

job("MNTLAB-" + NAME + "-main-build-job"){
  parameters{
    choiceParam('BRANCH_NAME', [NAME, 'main'], 'branch')
    activeChoiceParam('JOBA') {
      description('Allows user choose from multiple choices')
      choiceType('CHECKBOX')
        groovyScript {
          script('''def list = []
          (1..4).each {
            job_list.add('MNTLAB-''' + student + '''-child' + it + '-build-job')
          }
          return list''')
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
          predefinedProp('BRANCH_NAME', '$BRANCH_NAME')
        }                         
      }
    }
  }
}

[1, 2, 3, 4].each {
  job("MNTLAB-" + NAME + "-child" + it + "-build-job") {
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
      shell('''chmod +x script.sh && ./script.sh > output.txt
                tar czf ${BRANCH_NAME}_dsl_script.tar.gz *.sh *.groovy''')
    }
    publishers{
      archiveArtifacts{
        pattern('*.txt')
        pattern('${BRANCH_NAME}_dsl_script.tar.gz')
        onlyIfSuccessful()
      }
    }
  }
}
