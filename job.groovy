student="kkarpau"

job ("MNTLAB-${student}-main-build-job") {
  scm {
    github('MNT-Lab/dsl-task', "$student")
  }
  parameters {
    choiceParam('BRANCH_NAME', ["$student", 'main'])
    activeChoiceParam('JOB') {
      description('Select jobs')
      choiceType('CHECKBOX')
      groovyScript {
        script('''
          stud="kkarpau"
          def childlist=[]
          (1..4).each {
            childlist.add("MNTLAB-$stud-child" + it + "-build-job")
          }
          return childlist
        ''')
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

  [1, 2, 3, 4].each { num ->
    job("MNTLAB-${student}-child${num}-build-job") {
      parameters {
        activeChoiceParam('BRANCH_NAME') {
          choiceType('SINGLE_SELECT')
          groovyScript {
            script('''
            def gettags = ("git ls-remote -t -h https://github.com/MNT-Lab/dsl-task.git").execute()
            return gettags.text.readLines().collect {
              it.split()[1].replaceAll('refs/heads/', '')
            }
          ''')
          }
        }
      }
      scm {
        git {
          remote {
            url('https://github.com/MNT-Lab/dsl-task.git')
            branch("\${BRANCH_NAME}")
          }
        }
      }
      steps {
        shell('''
          chmod +x script.sh 
          ./script.sh > output.txt
          tar -czf ${BRANCH_NAME}_dsl_script.tar.gz output.txt  script.sh
          ''')
      }
      publishers {
        archiveArtifacts('${BRANCH_NAME}_dsl_script.tar.gz')
        archiveArtifacts('output.txt')
      }
    }
  }
}
