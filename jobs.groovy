[1, 2, 3, 4].each { num ->
  job("MNTLAB-vratomski-child${num}-build-job") {
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
          branch('${BRANCH_NAME}')
        }
      }
    }
      
    steps {
      shell('''sh script.sh > output.txt
               tar czf ${BRANCH_NAME}_dsl_script.tar.gz output.txt''')
    }
    
    publishers {
      archiveArtifacts('${BRANCH_NAME}_dsl_script.tar.gz')
    }
  }
}

job ('MNTLAB-vratomski-main-build-job') {
  parameters {
    choiceParam('BRANCH_NAME', ['vratomski', 'main'])
    activeChoiceParam('CHILD_JOBS') { 
      description('Choose jobs')
      choiceType('CHECKBOX')
      groovyScript {
        script('["MNTLAB-vratomski-child1-build-job", "MNTLAB-vratomski-child2-build-job", "MNTLAB-vratomski-child3-build-job", "MNTLAB-vratomski-child4-build-job"]')
        fallbackScript('"fallback choice"')
      }
    }
  }    
  
  steps {
    downstreamParameterized {
      trigger('${CHILD_JOBS}') {
        parameters {
          predefinedProp('BRANCH_NAME', '${BRANCH_NAME}')
        }
      }
    }
  }
}
