//
//  S5Connection.h
//  Session7
//
//  Created by Matthias Lübken on 24.07.09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface S5URL : NSObject {

}
- (id) initWithIp: (NSString*) ip1 andPort: (NSString*) port1;
- (NSURL*) url;
- (NSURL*) urlForImage: (int) imageNr;
- (NSURLRequest*) request ;
- (NSURLConnection*) call: (NSString*) command andDelegate: (id) delegate;
@end
