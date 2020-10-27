[1, 2, 3, 4].each {
    job('MNTLAB-aafanasenko-child' + it + '-build-job') {
        parameters {
            gitParameterDefinition {
                    name('BRANCH_NAME')
                	type('PT_BRANCH')
            		defaultValue('origin/main')
            		description('Name of the branch or tag to check out')
            		branch('')
            		branchFilter('origin/(.*)')
            		tagFilter('*')
            		sortMode('ASCENDING_SMART')
            		selectedValue('DEFAULT')
            		useRepository('')
            		quickFilterEnabled(false)
              		listSize('1')	
            }

        }
        scm {
            git {
                remote {
                    url('https://github.com/MNT-Lab/dsl-task')
                }
                branch("\${BRANCH_NAME}")
            }
        }
        steps {
            shell('''/bin/bash script.sh > output.txt 
            if [ -f jobs.groovy ]; then 
            tar czf ${BRANCH_NAME}_dsl_script.tar.gz jobs.groovy script.sh
            else tar czf ${BRANCH_NAME}_dsl_script.tar.gz script.sh 
            fi''')
        }
        publishers {
            archiveArtifacts {
                pattern('output.txt')
                pattern('${BRANCH_NAME}_dsl_script.tar.gz')
            }
        }
    }
}

job('MNTLAB-aafanasenko-main-job'){
    parameters{
        activeChoiceParam('NAMES') { 
            description('Allows user choose from multiple choices')
            filterable()
            choiceType('CHECKBOX')
            filterable(false)
            groovyScript {
                script('return ["MNTLAB-aafanasenko-child1-build-job", "MNTLAB-aafanasenko-child2-build-job", "MNTLAB-aafanasenko-child3-build-job", "MNTLAB-aafanasenko-child4-build-job"]')
                fallbackScript('"fallback choice"')
            }
        }
        choiceParam('BRANCH_NAME', ['aafanasenko', 'main'], 'choose branch')
    }
    steps{
        downstreamParameterized {
            trigger('\${NAMES}') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    currentBuild()  
                }
            }
        }
    }
}