
Pod::Spec.new do |s|
  s.name         = "RNFrequency"
  s.version      = "1.0.0"
  s.summary      = "RNFrequency"
  s.description  = <<-DESC
                  RNFrequency
                   DESC
  s.homepage     = "https://github.com/robinpowered/react-native-frequency"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNFrequency.git", :tag => "master" }
  s.source_files  = "RNFrequency/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  
